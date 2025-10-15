import subprocess
import time
import schedule
import random
from collector import fetch_proxies
from validator import validate_proxies

GOST_PATH = "/usr/local/bin/gost"  # Correct path as installed in Dockerfile
LISTEN_PORT = "8080"
VALID_PROXIES = []
GOST_PROCESS = None

def build_gost_command(proxies):
    """Builds the command to run gost with a list of proxies as a load balancer."""
    if not proxies:
        print("No valid proxies to serve.")
        return None

    # Create a load-balanced proxy chain
    # gost will listen on LISTEN_PORT and forward to one of the proxies
    forward_chain = " ".join([f"socks5://{p}" for p in proxies])

    command = [
        GOST_PATH,
        "-L", f"socks5://:{LISTEN_PORT}",
        "-F", forward_chain
    ]

    # Add round-robin strategy for load balancing
    if len(proxies) > 1:
        command.extend(["-F", "round"])

    return command

def start_gost_process(command):
    """Starts the gost process with the given command."""
    global GOST_PROCESS
    if GOST_PROCESS:
        print("Stopping existing gost process...")
        GOST_PROCESS.terminate()
        GOST_PROCESS.wait()

    if command:
        print(f"Starting gost with command: {' '.join(command)}")
        GOST_PROCESS = subprocess.Popen(command)
    else:
        print("Could not start gost: no valid command.")

def update_and_run():
    """
    Fetches, validates, and serves proxies. This is the main scheduled job.
    """
    global VALID_PROXIES
    print("\n--- Running scheduled proxy update ---")

    # 1. Fetch proxies
    all_proxies = fetch_proxies()
    random.shuffle(all_proxies)

    # 2. Validate proxies
    # We aim for 220, but will proceed with whatever is found
    print("Validating proxies...")
    # Taking a larger sample to increase the chances of finding enough valid proxies
    sample_size = min(len(all_proxies), 2000)
    current_valid_proxies = validate_proxies(all_proxies[:sample_size], num_to_find=220, batch_size=50)

    if not current_valid_proxies:
        print("No valid proxies found in this run. Keeping the old list if available.")
        if not VALID_PROXIES:
            # If there are no old proxies, stop the service
            if GOST_PROCESS:
                GOST_PROCESS.terminate()
            print("Service stopped due to lack of valid proxies.")
        return

    # 3. Update proxy list and restart gost
    VALID_PROXIES = current_valid_proxies
    print(f"Updated valid proxy list: {VALID_PROXIES}")

    gost_command = build_gost_command(VALID_PROXIES)
    start_gost_process(gost_command)

def main():
    """
    Main function to start the scheduler.
    """
    print("--- Proxy Service Starting ---")

    # Run the job once at the start
    update_and_run()

    # Schedule the job to run every 10 minutes
    schedule.every(10).minutes.do(update_and_run)

    print("Scheduler started. Will update proxies every 10 minutes.")
    while True:
        schedule.run_pending()
        time.sleep(1)

if __name__ == "__main__":
    # Note: This script assumes 'gost' is available in the execution path.
    # The Dockerfile will handle the download and placement of the gost binary.
    main()
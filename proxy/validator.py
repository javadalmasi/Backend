import requests
import socks
import socket
import threading
from queue import Queue

# Countries where YouTube is blocked or heavily restricted
BLOCKED_COUNTRIES = {"CN", "IR", "KP", "ER", "SS", "SY", "TJ", "TM"}

# Cloudflare trace URL
CF_TRACE_URL = "https://cloudflare.com/cdn-cgi/trace"
YOUTUBE_URL = "https://www.youtube.com"

def parse_trace_data(trace_data):
    """Parses the data from cloudflare.com/cdn-cgi/trace."""
    data = {}
    for line in trace_data.strip().split("\n"):
        if "=" in line:
            key, value = line.split("=", 1)
            data[key] = value
    return data

def check_proxy(proxy, result_queue):
    """
    Checks a single proxy for validity.
    Puts the proxy into the result_queue if it's valid.
    """
    try:
        ip, port = proxy.split(":")
        port = int(port)

        # Configure requests to use the SOCKS5 proxy
        session = requests.Session()
        session.proxies = {
            'http': f'socks5h://{ip}:{port}',
            'https': f'socks5h://{ip}:{port}'
        }

        # 1. Check Cloudflare trace
        try:
            trace_resp = session.get(CF_TRACE_URL, timeout=15)
            trace_resp.raise_for_status()
            trace_data = parse_trace_data(trace_resp.text)

            # 2. Check country and Tor status
            country = trace_data.get("loc")
            is_tor = trace_data.get("t1") == "1"

            if not country or country in BLOCKED_COUNTRIES or is_tor:
                # Using a silent return is better for performance than printing
                return
        except requests.RequestException:
            # Most proxies will fail here, no need to print
            return

        # 3. Check YouTube accessibility
        try:
            yt_resp = session.get(YOUTUBE_URL, timeout=15, headers={'User-Agent': 'Mozilla/5.0'})
            yt_resp.raise_for_status()
            # A successful status code is enough to validate
        except requests.RequestException:
            return

        # If all checks pass, add to the valid list
        result_queue.put(proxy)

    except Exception:
        # Catches potential errors like invalid proxy format or socket errors
        pass

def validate_proxies(proxies, num_to_find=220, batch_size=50):
    """
    Validates a list of proxies in batches using multithreading.

    Args:
        proxies (list): A list of proxy strings (e.g., "1.2.3.4:5678").
        num_to_find (int): The target number of valid proxies to find.
        batch_size (int): The number of proxies to check concurrently.

    Returns:
        A list of valid proxies.
    """
    valid_proxies = []
    proxy_queue = Queue()
    for p in proxies:
        proxy_queue.put(p)

    result_queue = Queue()

    print(f"Starting validation to find {num_to_find} proxies...")
    while len(valid_proxies) < num_to_find and not proxy_queue.empty():
        threads = []
        current_batch_size = min(batch_size, proxy_queue.qsize())

        for _ in range(current_batch_size):
            proxy = proxy_queue.get()
            thread = threading.Thread(target=check_proxy, args=(proxy, result_queue))
            threads.append(thread)
            thread.start()

        for t in threads:
            t.join() # Wait for all threads in the batch to complete

        while not result_queue.empty():
            valid_proxy = result_queue.get()
            valid_proxies.append(valid_proxy)
            print(f"Found valid proxy: {valid_proxy}")

        print(f"Progress: {len(valid_proxies)}/{num_to_find} valid proxies found.")

    print(f"Validation finished. Total valid proxies: {len(valid_proxies)}.")
    return valid_proxies[:num_to_find]

if __name__ == "__main__":
    from collector import fetch_proxies
    import random

    print("Loading predefined proxy list...")
    all_proxies = fetch_proxies()

    if not all_proxies:
        print("No proxies loaded. Exiting.")
    else:
        print(f"Loaded {len(all_proxies)} proxies. Starting validation...")

        # Validate the list of 10 proxies to find 5 working ones
        validated = validate_proxies(all_proxies, num_to_find=5, batch_size=10)

        print(f"\n--- Found {len(validated)} Valid Proxies ---")
        for proxy in validated:
            print(proxy)
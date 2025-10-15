import requests
import re

# List of URLs for SOCKS5 proxy lists
PROXY_LIST_URLS = [
    "https://raw.githubusercontent.com/hookzof/socks5_list/master/proxy.txt",
    "https://raw.githubusercontent.com/r00tee/Proxy-List/main/Socks5.txt",
    "https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/socks5.txt",
    "https://raw.githubusercontent.com/jetkai/proxy-list/main/online-proxies/txt/proxies-socks5.txt",
    "https://raw.githubusercontent.com/mmpx12/proxy-list/master/socks5.txt",
    "https://raw.githubusercontent.com/monosans/proxy-list/main/proxies/socks5.txt",
    "https://raw.githubusercontent.com/zevtyardt/proxy-list/main/socks5.txt",
    "https://raw.githubusercontent.com/prxchk/proxy-list/main/socks5.txt"
]

# Regex to match IP:PORT format
IP_PORT_REGEX = re.compile(r"\b(?:\d{1,3}\.){3}\d{1,3}:\d{1,5}\b")

def fetch_proxies():
    """
    Fetches proxies from all URLs and returns a set of unique proxies.
    """
    proxies = set()
    for url in PROXY_LIST_URLS:
        try:
            response = requests.get(url, timeout=10)
            response.raise_for_status()
            found_proxies = IP_PORT_REGEX.findall(response.text)
            proxies.update(found_proxies)
            print(f"Successfully fetched {len(found_proxies)} proxies from {url}")
        except requests.RequestException as e:
            print(f"Could not fetch or read proxies from {url}: {e}")
    return list(proxies)

if __name__ == "__main__":
    unique_proxies = fetch_proxies()
    print(f"\nTotal unique SOCKS5 proxies found: {len(unique_proxies)}")
    # For demonstration, print the first 20 proxies
    for proxy in unique_proxies[:20]:
        print(proxy)
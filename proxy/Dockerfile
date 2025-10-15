# Use a different Python image to avoid Docker Hub rate limits
FROM python:3.9-bullseye

# Set the working directory
WORKDIR /app

# Install dependencies for Python and gost
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    gzip \
    && rm -rf /var/lib/apt/lists/*

# Install Python libraries
COPY proxy/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Download and install gost
ARG GOST_VERSION=2.11.5
RUN wget "https://github.com/ginuerzh/gost/releases/download/v${GOST_VERSION}/gost-linux-amd64-${GOST_VERSION}.gz" && \
    gunzip "gost-linux-amd64-${GOST_VERSION}.gz" && \
    mv "gost-linux-amd64-${GOST_VERSION}" /usr/local/bin/gost && \
    chmod +x /usr/local/bin/gost

# Copy the proxy application files
COPY proxy/ /app/

# Set the entrypoint
CMD ["python3", "main.py"]
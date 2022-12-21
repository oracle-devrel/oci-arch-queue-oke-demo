FROM python:3.11-buster
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
# COPY config oci_api_key.pem queue_automation.py ratemate.py logging.conf ./
COPY source/queue_automation.py source/ratemate.py source/logging.conf ./
ENTRYPOINT ["python3", "queue_automation.py"]

FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1

WORKDIR /app

COPY requirements.txt ./requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

COPY train_traffic_signs.py ./train_traffic_signs.py
COPY traffic_signs.yaml ./traffic_signs.yaml
COPY worker.py ./worker.py

RUN mkdir -p /app/runtime/training

CMD ["python", "worker.py"]

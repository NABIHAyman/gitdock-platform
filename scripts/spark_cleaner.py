import logging
from pyspark.sql import SparkSession
from pyspark.sql.functions import col, from_json, to_json, struct, length
from pyspark.sql.types import StructType, StructField, StringType

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')
logger = logging.getLogger(__name__)

# --- 1. INITIALISATION DE SPARK ---
# On inclut le package Kafka pour Spark
spark = SparkSession.builder \
    .appName("GitDock-Spark-Cleaner") \
    .config("spark.jars.packages", "org.apache.spark:spark-sql-kafka-0-10_2.13:4.1.1") \
    .getOrCreate()

# Garder les logs Spark silencieux (seulement les WARN/ERROR)
spark.sparkContext.setLogLevel("WARN")
logger.info("⚡ Apache Spark Streaming initialisé !")

# --- 2. DÉFINITION DU SCHÉMA JSON ---
schema = StructType([
    StructField("hash", StringType(), True),
    StructField("project_id", StringType(), True),
    StructField("diff", StringType(), True),
    StructField("author", StringType(), True),
    StructField("source", StringType(), True)
])

# --- 3. LECTURE DU FLUX BRUT (raw-commits) ---
logger.info("🎧 Écoute du topic [raw-commits]...")
raw_df = spark.readStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", "127.0.0.1:9092") \
    .option("subscribe", "raw-commits") \
    .load()

# Extraction des données JSON de la colonne "value" de Kafka
parsed_df = raw_df.select(from_json(col("value").cast("string"), schema).alias("data")).select("data.*")

# --- 4. LA LOGIQUE DE NETTOYAGE (CLEANING) ---
cleaned_df = parsed_df \
    .filter(col("diff").isNotNull()) \
    .filter(length(col("diff")) > 10) \
    .filter(length(col("diff")) < 15000) \
    .filter(~col("author").contains("[bot]"))

# --- 5. ÉCRITURE DANS LE FLUX PROPRE (cleaned-commits) ---
logger.info("📤 Redirection vers le topic [cleaned-commits]...")
query = cleaned_df.select(to_json(struct("*")).alias("value")) \
    .writeStream \
    .format("kafka") \
    .option("kafka.bootstrap.servers", "127.0.0.1:9092") \
    .option("topic", "cleaned-commits") \
    .option("checkpointLocation", "/tmp/spark-checkpoints/gitdock-cleaner") \
    .start()

query.awaitTermination()
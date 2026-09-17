"""
Synthetic training data generator for Shadow Sentinel classification model.
Produces 2000 labeled rows adhering to rules documented in docs/labeling-rules.md.
"""

import os
import random
import pandas as pd

FEATURE_COLUMNS = [
    "domainLength",
    "visitCount",
    "durationSeconds",
    "isKnownAiDomain",
    "hourOfDay",
    "pathDepth",
    "chatInterfacePresent",
    "promptInputPresent",
    "generateControlPresent",
    "regenerateControlPresent",
    "aiTermCount",
    "streamingOutputPresent",
    "fileUploadPresent",
    "promptSubmitCount",
    "generateClickCount",
    "pasteEventCount",
    "copyFromResponseCount",
    "typedCharCountBucket"
]


def generate_non_ai():
    return {
        "domainLength": random.randint(8, 25),
        "visitCount": random.randint(1, 40),
        "durationSeconds": random.randint(5, 600),
        "isKnownAiDomain": False,
        "hourOfDay": random.randint(0, 23),
        "pathDepth": random.randint(0, 5),
        "chatInterfacePresent": False,
        "promptInputPresent": random.random() < 0.05,  # rare false positive search inputs
        "generateControlPresent": False,
        "regenerateControlPresent": False,
        "aiTermCount": 1 if random.random() < 0.04 else 0,
        "streamingOutputPresent": False,
        "fileUploadPresent": random.random() < 0.05,
        "promptSubmitCount": 0,
        "generateClickCount": 0,
        "pasteEventCount": 1 if random.random() < 0.05 else 0,
        "copyFromResponseCount": 0,
        "typedCharCountBucket": 1 if random.random() < 0.05 else 0,
        "label": "NON_AI"
    }


def generate_ai_capable():
    is_known = random.random() < 0.85
    return {
        "domainLength": random.randint(10, 22),
        "visitCount": random.randint(1, 25),
        "durationSeconds": random.randint(5, 120),
        "isKnownAiDomain": is_known,
        "hourOfDay": random.randint(0, 23),
        "pathDepth": random.randint(0, 3),
        "chatInterfacePresent": True if is_known or random.random() < 0.75 else False,
        "promptInputPresent": True if is_known or random.random() < 0.80 else False,
        "generateControlPresent": random.random() < 0.75,
        "regenerateControlPresent": random.random() < 0.30,
        "aiTermCount": random.randint(2, 12),
        "streamingOutputPresent": False,
        "fileUploadPresent": random.random() < 0.35,
        # Strictly zero user engagement
        "promptSubmitCount": 0,
        "generateClickCount": 0,
        "pasteEventCount": 0,
        "copyFromResponseCount": 0,
        "typedCharCountBucket": 0,
        "label": "AI_CAPABLE_PAGE"
    }


def generate_ai_interaction():
    return {
        "domainLength": random.randint(10, 24),
        "visitCount": random.randint(2, 35),
        "durationSeconds": random.randint(30, 450),
        "isKnownAiDomain": random.random() < 0.80,
        "hourOfDay": random.randint(0, 23),
        "pathDepth": random.randint(1, 4),
        "chatInterfacePresent": True,
        "promptInputPresent": True,
        "generateControlPresent": True,
        "regenerateControlPresent": random.random() < 0.35,
        "aiTermCount": random.randint(3, 15),
        # Crucial: No output consumption yet
        "streamingOutputPresent": False,
        "fileUploadPresent": random.random() < 0.40,
        # Active user input
        "promptSubmitCount": random.randint(1, 4),
        "generateClickCount": random.randint(0, 3),
        "pasteEventCount": random.randint(0, 3),
        "copyFromResponseCount": 0,
        "typedCharCountBucket": random.randint(1, 3),
        "label": "AI_INTERACTION"
    }


def generate_ai_generation():
    has_stream = random.random() < 0.80
    has_copy = True if not has_stream else (random.random() < 0.65)

    return {
        "domainLength": random.randint(10, 25),
        "visitCount": random.randint(3, 50),
        "durationSeconds": random.randint(60, 900),
        "isKnownAiDomain": random.random() < 0.85,
        "hourOfDay": random.randint(0, 23),
        "pathDepth": random.randint(1, 4),
        "chatInterfacePresent": True,
        "promptInputPresent": True,
        "generateControlPresent": True,
        "regenerateControlPresent": random.random() < 0.70,
        "aiTermCount": random.randint(4, 20),
        "streamingOutputPresent": has_stream,
        "fileUploadPresent": random.random() < 0.45,
        # Prompt submitted / generated AND output consumed
        "promptSubmitCount": random.randint(1, 8),
        "generateClickCount": random.randint(1, 8),
        "pasteEventCount": random.randint(0, 5),
        "copyFromResponseCount": random.randint(1, 6) if has_copy else 0,
        "typedCharCountBucket": random.randint(2, 4),
        "label": "AI_GENERATION"
    }


def main():
    random.seed(42)
    generators = [
        (generate_non_ai, 500),
        (generate_ai_capable, 500),
        (generate_ai_interaction, 500),
        (generate_ai_generation, 500)
    ]

    rows = []
    for gen, count in generators:
        for _ in range(count):
            rows.append(gen())

    # Shuffle rows
    random.shuffle(rows)

    df = pd.DataFrame(rows)

    output_dir = os.path.join(os.path.dirname(__file__), "..", "data")
    os.makedirs(output_dir, exist_ok=True)
    csv_path = os.path.join(output_dir, "training.csv")

    df.to_csv(csv_path, index=False)
    print(f"Generated {len(df)} synthetic rows saved to {csv_path}")
    print("\nClass distribution:")
    print(df["label"].value_counts())


if __name__ == "__main__":
    main()

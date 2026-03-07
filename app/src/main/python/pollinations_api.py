from io import BytesIO
from typing import Dict, Tuple
import base64
import requests

def upload_image_to_imgbb(image_bytes: bytes, imgbb_key: str, expiration: int = 3600) -> str:
    image_base64 = base64.b64encode(image_bytes).decode("utf-8")
    resp = requests.post(
        "https://api.imgbb.com/1/upload",
        data={
            "key": imgbb_key,
            "image": image_base64,
            "expiration": expiration
        }
    )
    resp.raise_for_status()
    data = resp.json()["data"]
    print(data)
    return data["url"]

def recognize_test_answers(prompt: str, page_bytes: bytes, imgbb_key: str, pollinations_key: str) -> str:
    image_url = upload_image_to_imgbb(page_bytes, imgbb_key, expiration=3600)
    data = {
        "model": "gemini-fast",
        "messages": [
            {
                "role": "user",
                "content": [
                    {"type": "text", "text": prompt},
                    {
                        "type": "image_url",
                        "image_url": {"url": image_url}
                    }
                ]
            }
        ]
    }
    headers = {
        "Authorization": f"Bearer {pollinations_key}",
        "Content-Type": "application/json"
    }
    result = ""
    try:
        response = requests.post(
            "https://gen.pollinations.ai/v1/chat/completions",
            headers=headers,
            json=data,
            timeout=60
        )
        result = response.json()["choices"][0]["message"]["content"]
        print(result)
    except Exception as e:
        print(str(e))
        result = f"Ошибка:{str(e)}"
    finally:
        return result

def get_test_answers(page_bytes, task_types, imgbb_key, pollinations_key):
    prompt = f"""
        На изображении представлены ответы ученика на тестовые задания.
        Типы допустимых ответов:
        {task_types}
        Алгоритм:
        1. Найди все номера тестовых заданий на странице.
        2. Для каждого номера найди отмеченный ответ. Ответ должен соответствовать указанному типу.
        3. Игнорируй зачёркнутые ответы.
        4. Игнорируй задания с развёрнутым ответом.
        5. Если ответ отсутствует, но номер задания есть — выведи только номер.
        Формат строки:
        <номер><пробел><ответ>
        Требования:
        - внутри ответа не должно быть пробелов
        - единственный пробел — между номером задания и ответом
        - каждый ответ выводится на новой строке
        - не выводи ничего кроме ответов
        Пример вывода:
        1 А
        2 Б
        3
        4 Г
    """
    try:
        print(1)
        return recognize_test_answers("".join(prompt), page_bytes, imgbb_key, pollinations_key)
    except Exception as e:
        print(str(e))
        return f"{str(e).replace(" ", "")}"

def check_detailed_task(pages_bytes: list, criteria_bytes: list, task_num: int, imgbb_key: str, pollinations_key: str) -> Dict[str, Tuple[int, str]]:
    prompt = [
        f"Ты - преподаватель, проверяющий работы учеников с развёрнутыми ответами, а конкретно - задание {task_num}.\n",
        "Тебе предоставлены все страницы работы ученика, эталонный ответ (если есть) и критерии оценивания. Необходимо найти нужное задание и очень внимательно проверить его.\n",
        "Алгоритм проверки:\n",
        "1. Найди ответ на задание. Если его нет, то по всем критериям 0\n",
        "2. ВНИМАТЕЛЬНО изучи ответ ученика и приведённую систему критериев оценивания\n",
        "3. Выбери ОДИН наиболее подходящий вариант оценки из для каждого критерия\n",
        "4. Формат вывода: Номер критерия|Баллы|Комментарий\n",
        "5. Под критерием понимается пункт в оценивании, в котором может быть выставленно разное количество баллов\n",
        "6. Не придумывай критерии, используй только те, что на изображении.\n",
        "Требования:\n",
        "- Выбери только ОДИН подходящий вариант оценивания для каждого критерия.\n",
        "- В комментарии укажи, почему выбран именно он. Ограничься 1-2 предложениями.\n"
    ]

    try:
        return get_detailed_task("".join(prompt), pages_bytes, criteria_bytes, imgbb_key, pollinations_key)
    except Exception as e:
        print(str(e))
        return f"Ошибка обработки: {str(e)}"

def get_detailed_task(prompt: str, pages_bytes: list, criteria_bytes: list, imgbb_key: str, pollinations_key: str) -> Dict[str, Tuple[int, str]]:
    images = []
    for page in pages_bytes:
        url = upload_image_to_imgbb(page, imgbb_key)
        images.append({
            "type": "image_url",
            "image_url": {"url": url}
        })
    for page in criteria_bytes:
        url = upload_image_to_imgbb(page, imgbb_key)
        images.append({
            "type": "image_url",
            "image_url": {"url": url}
        })
    message_content = [{"type": "text", "text": prompt}] + images
#     for i, page in enumerate(pages_bytes):
#         images.append([BytesIO(page), f"work_page_{i}.jpg"])
#     for i, page in enumerate(criteria_bytes):
#         images.append([BytesIO(page), f"criteria_page_{i}.jpg"])
    data = {
        "model": "openai",
        "messages": [
            {
                "role": "user",
                "content": message_content
            }
        ],
        "temperature": 0
    }
    headers = {
        "Authorization": f"Bearer {pollinations_key}",
        "Content-Type": "application/json"
    }
    response = requests.post(
        "https://gen.pollinations.ai/v1/chat/completions",
        headers=headers,
        json=data,
        timeout=60
    )
    return parse_response(response.json()["choices"][0]["message"]["content"])

def parse_response(text: str) -> Dict[str, Tuple[int, str]]:
    result = {}
    current_criterion = None
    current_score = None
    comment_lines = []
    for line in text.split('\n'):
        line = line.strip()
        if '|' in line:
            if current_criterion is not None:
                result[current_criterion] = (current_score, ' '.join(comment_lines))
            parts = line.split('|')
            current_criterion = parts[0].strip()
            try:
                current_score = int(parts[1].strip())
            except (IndexError, ValueError):
                current_score = 0
            comment_lines = [parts[2].strip()] if len(parts) > 2 else []
        elif current_criterion is not None:
            comment_lines.append(line)
    if current_criterion is not None:
        result[current_criterion] = (current_score, ' '.join(comment_lines))
    return result
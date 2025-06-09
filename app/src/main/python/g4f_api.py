import g4f
import g4f.Provider
from io import BytesIO
from typing import Dict, Tuple

def recognize_test_answers(prompt, page_bytes):
    client = g4f.Client(provider=g4f.Provider.PollinationsAI)
    with BytesIO(page_bytes) as img_buffer:
        images = [
            [img_buffer, "Страница.jpg"]
        ]
        response = client.chat.completions.create(
            messages=[{"content": prompt, "role": "user"}],
            model="",
            images=images
        )
        return response.choices[0].message.content

def get_test_answers(page_bytes, taskTypes):
    prompt = [
        "На этом изображении представлены ответы на тестовые задания. Распознай их и выведи в формате <номер ответ> (один пробел между ними), каждый блок на отдельной строке.\n",
        "Ниже каждому заданию сопоставлены характеристики ответов, которые ожидаются в качестве правильного ответа, если ответ вообще есть:\n",
        taskTypes,
        "Задания с развёрнутым ответом выше не перечислены. Такие задания распознавать не нужно, если таковые есть на странице.\n",
        "Зачёркивания не должны распознаваться и не должны влиять на предсказания.\n",
        "Может быть такое, что для какого-то задания нет ответа. Если ответа нет, но есть номер, то выведи номер.\n",
        "Внутри ответов не должно быть пробелов. Единственный пробел - после номера задания.\n",
        "Кроме этого больше ничего не выводи, в том числе заголовок"
    ]

    try:
        return recognize_test_answers("".join(prompt), page_bytes)
    except Exception as e:
        return f"Ошибка обработки: {str(e)}"

def check_detailed_task(pages_bytes: list, criteria_bytes: list, taskNum: int) -> Dict[str, Tuple[int, str]]:
    prompt = [
        f"Ты - преподаватель, проверяющий работы учеников с развёрнутыми ответами, а конкретно - задание {taskNum}.\n",
        "Тебе предоставлены все страницы работы ученика, эталонный ответ (если есть) и критерии оценивания. Необходимо найти нужное задание и проверить его.\n",
        "Инструкция по проверке:\n",
        "1. ВНИМАТЕЛЬНО изучи ответ ученика на задание и приведённую систему критериев оценивания\n",
        "2. Выбери ОДИН наиболее подходящий вариант оценки из системы критериев\n",
        "3. Формат вывода: Номер критерия|Баллы|Комментарий\n",
        "Требования:\n",
        "- Выбери только ОДИН подходящий критерий из системы.\n",
        "- В комментарии укажи, почему выбран именно этот критерий\n"
    ]

    try:
        return get_detailed_task("".join(prompt), pages_bytes, criteria_bytes)
    except Exception as e:
        return f"Ошибка обработки: {str(e)}"

def get_detailed_task(prompt: str, pages_bytes: list, criteria_bytes: list) -> Dict[str, Tuple[int, str]]:
    client = g4f.Client(provider=g4f.Provider.PollinationsAI)
    images = []
    for i, page in enumerate(pages_bytes):
        images.append([BytesIO(page), f"work_page_{i}.jpg"])
    for i, page in enumerate(criteria_bytes):
        images.append([BytesIO(page), f"criteria_page_{i}.jpg"])
    response = client.chat.completions.create(
        messages=[{"content": prompt, "role": "user"}],
        model="",
        temperature=0,
        images=images
    )
    return parse_response(response.choices[0].message.content)

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
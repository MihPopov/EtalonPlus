import g4f
import g4f.Provider
from io import BytesIO

def chat_completion(prompt, image_bytes):
    client = g4f.Client(provider=g4f.Provider.PollinationsAI)
    with BytesIO(image_bytes) as img_buffer:
        images = [
            [img_buffer, "input_image.jpg"]
        ]
        response = client.chat.completions.create(
            messages=[{"content": prompt, "role": "user"}],
            model="",
            images=images
        )
        return response.choices[0].message.content

def process_image(image_bytes, taskTypes):
    prompt = [
        "На этом изображении представлены ответы на тестовые задания. Распознай их и выведи в формате <номер ответ> (один пробел между ними), каждый блок на отдельной строке.\n",
        "Ниже каждому заданию сопоставлены характеристики ответов, которые ожидаются в качестве правильного ответа, если ответ вообще есть:\n",
        taskTypes,
        "Зачёркивания не должны распознаваться и не должны влиять на предсказания.\n",
        "Может быть такое, что для какого-то задания нет ответа. Если ответа нет, но есть номер, то выведи номер.\n",
        "Внутри ответов не должно быть пробелов. Единственный пробел - после номера задания.\n",
        "Кроме этого больше ничего не выводи, в том числе заголовок"
    ]

    try:
        return chat_completion("".join(prompt), image_bytes)
    except Exception as e:
        return f"Ошибка обработки: {str(e)}"
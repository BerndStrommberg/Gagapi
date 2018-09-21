# Server use
...

# Rasperry Pi 

## Accessing the Server Api
Der Server kann ganz einfach mit einem http get Request angespochen werden. Dabei sollte er die Form http://{server-ip}:8080/?action=present&data=none haben. In Python kann das mit der request Library gemacht werden:
r = requests.get("http://localhost:8080/?action=present&data=non")
print(r.text)

## Programming the Lights
[link]https://www.pythonforbeginners.com/requests/using-requests-in-python[/link]
request.put("http://{ip-adresse}:{port}/api/request)

r.text
r.json


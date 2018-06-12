import bluetooth
import io
import sys
from mouse import Mouse

mbot = Mouse()

service_id ="0b938f75-7fe7-4a04-9079-579d78ad64b7"
service_classes = [service_id, bluetooth.SERIAL_PORT_CLASS]
profiles = [bluetooth.SERIAL_PORT_PROFILE]

def setup() :
    serverSocket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    port = bluetooth.PORT_ANY
    serverSocket.bind(("",port))
    serverSocket.listen(1)
    port=serverSocket.getsockname()[1]

    bluetooth.advertise_service(serverSocket, "Mouse", service_id, service_classes, profiles)
    global inputSocket
    inputSocket, address = serverSocket.accept()
    print "connected"

def close() :
    inputSocket.close()
    serverSocket.close()

setup()
# i = input()
while True:
    input = inputSocket.recv(1024)
    data = bytearray(input)
    # for b in data :
    #     print b
    x = int(data[0])
    y = int(data[1])
    mbot.move(x,y)
    print x, y
close()
# mbot.move(10,10)

import bluetooth
import io

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
while True:
    input = inputSocket.recv(1024)
    data = bytes((input,'UTF-8'))
    print data.decode()

    test = io.BytesIO(input)
    print test.getvalue()
close()

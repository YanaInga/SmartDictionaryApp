import socket
import sys
import NeuralNetwork

BUFSIZE=2048

#HOST = '192.168.3.9'
HOST = '192.168.243.209'
PORT = 8888

tcpServerSocket=socket.socket(socket.AF_INET, socket.SOCK_STREAM)# Создать объект сокета
print('socket created')

try:
    tcpServerSocket.bind((HOST, PORT))
except socket.error as err:
    print('Bind Failed, Error Code: ' + str(err[0]) + ', Message: ' + err[1])
    sys.exit()

print('Socket Bind Success!')
tcpServerSocket.listen(5) #Максимальное количество очередей, ожидающих соединения в событиях агента.
print('Socket is now listening')
while True:
    print("В ожидании соединения")
    # Установить клиентское соединение, принять соединение и вернуть два параметра, c - новый объект сокета, который может отправлять и получать данные о соединении
    #addr - это адрес, связанный с сокетом на другом конце соединения
    clientSocket, addr = tcpServerSocket.accept()
    print ("Адрес подключения:", addr)
    data= ""
    while True:
        try:
            data1 = clientSocket.recv(BUFSIZE).decode('utf8', errors='ignore')
            data = data + data1
            if "outMsg" in data:
                data = data.replace("outMsg", "")
                break
        except Exception as ex:
            template = "An exception of type {0} occurred. Arguments:\n{1!r}"
            message = template.format(type(ex).__name__, ex.args)
            print(message)
            break
    if not data:
        clientSocket.close()
    print("send for prediction")
    try:
        str = NeuralNetwork.predict(data)
        clientSocket.send(str.encode())# Строка, закодированная в байтах
    except Exception as ex:
        template = "An exception of type {0} occurred. Arguments:\n{1!r}"
        message = template.format(type(ex).__name__, ex.args)
        print(message)
        clientSocket.send("\n".encode())
    clientSocket.close()
    print("answer")

import socket
import sys  
import struct
import time
from thread import *


#Function for handling connections
def clientthread(conn):
    #Sending message to connected client
    conn.send(b'Welcome to the server....\n') #send only takes string

    #infinite loop so that function do not terminate and thread do not end.
    while True:

        #Receiving from client
        data = conn.recv(1024)
        reply = (b'Message Received at the server!\n')
        print(data)
        if not data:
            break

        conn.sendall(reply)

    conn.close()

def startMiniServerTCP(port=8888, HOST=''):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print('Socket created')

    try:
        s.bind((HOST, port))
    except (socket.error , msg):
        print('Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1])       
        sys.exit()

    print('Socket bind complete')
    s.listen(10)
    print('Socket now listening')

        # keep talking with the client
    while 1:
        #wait to accept a connection
        conn, addr = s.accept()
        print('Connected with ' + addr[0] + ':' + str(addr[1]))
       clientthread(conn)

    s.close()

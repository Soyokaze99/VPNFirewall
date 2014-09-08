import socket


def startMiniServer(port=5000, addr='localhost'):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.bind(("", port))
    print("waiting on port:", port)
    while 1:
        (data, addr) = s.recvfrom(1024)
        if not data: 
            break
        print(data)
        reply = b'OK...' + data
        s.sendto(reply , addr)
        print('Message[')
        print(addr[0])
        print(addr[1])
        print(data.strip())
     
    s.close()


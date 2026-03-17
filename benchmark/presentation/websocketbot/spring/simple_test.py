#!/usr/bin/env python3
"""
Simple WebSocket test to debug connection issues
"""
import socket
import base64
import os
from urllib.parse import urlparse

def test_websocket_connection():
    """Test basic WebSocket connection"""
    url = "ws://localhost:8080/websocketbot"
    pu = urlparse(url)
    host = pu.hostname
    port = pu.port or 80
    path = pu.path or "/"
    
    print(f"Connecting to {host}:{port}{path}")
    
    # Create socket connection
    sock = socket.create_connection((host, port), timeout=10)
    print("TCP connection established")
    
    # WebSocket handshake
    key = base64.b64encode(os.urandom(16)).decode()
    headers = [
        f"GET {path} HTTP/1.1",
        f"Host: {host}:{port}",
        "Upgrade: websocket",
        "Connection: Upgrade",
        f"Sec-WebSocket-Key: {key}",
        "Sec-WebSocket-Version: 13",
        "",
        ""
    ]
    
    handshake = "\r\n".join(headers)
    print(f"Sending handshake:\n{handshake}")
    sock.sendall(handshake.encode())
    
    # Read response
    response = b""
    while b"\r\n\r\n" not in response:
        chunk = sock.recv(4096)
        if not chunk:
            break
        response += chunk
        print(f"Received chunk: {chunk}")
    
    header, leftover = response.split(b"\r\n\r\n", 1)
    print(f"Response header: {header.decode()}")
    
    if b" 101 " in header:
        print("WebSocket handshake successful!")
        print(f"Leftover data: {leftover}")
        
        # Try to send a simple message
        join_msg = '{"type": "join", "name": "TestUser"}'
        print(f"Sending message: {join_msg}")
        
        # Simple WebSocket frame
        payload = join_msg.encode('utf-8')
        length = len(payload)
        header = bytes([0x81, length]) + payload
        sock.sendall(header)
        
        # Try to read response
        sock.settimeout(5)
        try:
            response = sock.recv(4096)
            print(f"Received response: {response}")
        except socket.timeout:
            print("No response received")
        except Exception as e:
            print(f"Error reading response: {e}")
            
    else:
        print("WebSocket handshake failed!")
        print(f"Response: {header.decode()}")
    
    sock.close()
    print("Connection closed")

if __name__ == "__main__":
    test_websocket_connection()

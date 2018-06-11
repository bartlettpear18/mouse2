import win32api

move = 0x0001
scroll = 0x0800
left = [0x0002, 0x0004]
right = [0x0008,0x0010]

class Mouse :
    current = [0,0]

    def __init__(self) :
        self.current = win32api.GetCursorPos()

    def move(self, a, b) :
        x = self.current[0] + a
        y = self.current[1] + b
        win32api.SetCursorPos((int(x), int(y)))
        self.current = win32api.GetCursorPos()

    def leftDown(self) : win32api.mouse_event(left[0],0,0,0,0)
    def leftUp(self) : win32api.mouse_event(left[1],0,0,0,0)
    def rightDown(self) : win32api.mouse_event(right[0],0,0,0,0)
    def rightUp(self) : win32api.mouse_event(right[1],0,0,0,0)
    def scroll(self, dis) : win32api.mouse_event(scroll, 0,0,int(dis),0)

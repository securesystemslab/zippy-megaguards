a = [1, 2, 3]
c = [0, 0, 0]
def t():
    for i in range(len(a)):
        x = a[i]*2
        c[i] = x
        
    while(x > 0):
        x -= 1

t()
print(c)

a = [1, 2, 3]
c = [0, 0, 0]

def w(x):
    return 2**7

def t():
    for i in range(len(a)):
        x = w(a[i])
        c[i] = x
t()
print(c)

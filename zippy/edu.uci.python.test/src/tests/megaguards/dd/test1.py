a = [1, 2, 3]
c = [0, 0, 0]
def t():
    for i in range(len(a)):
        c[i] = a[i]*2
t()
print(c)

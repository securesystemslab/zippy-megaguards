a = [1, 2, 3]
c = [0, 0, 0]

def t():
    for i in range(len(a)):
        for j in range(1,len(a)):
            c[i] = a[j]*2 + c[i - 1]
t()
print(c)

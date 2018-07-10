a = [1, 2, 3]
b = [4, 5, 6]
c = [0, 0, 0]
def t():
    for j in range(len(a)):
        for i in range(len(b)):
            c[j] = b[i] + a[j]*2
t()
print(c)

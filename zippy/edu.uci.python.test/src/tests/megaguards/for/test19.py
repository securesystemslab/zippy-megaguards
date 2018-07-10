a = [1, 2, 3]
b = [4, 5, 6]
c = [0, 0, 0]
def t():
    x = 2
    for j in range(len(a)):
        for i in range(x + 1):
            c[j] = b[i] + a[j]*2
t()
print(c)

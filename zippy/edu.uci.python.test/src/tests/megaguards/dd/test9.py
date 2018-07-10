a = [1, 2, 3]
c = [0, 0, 0]
def t():
    n = 0
    for i in range(len(a)):
        for j in range(len(a)):
            c[i] = a[n]*2
        n += 1;
t()
print(c)

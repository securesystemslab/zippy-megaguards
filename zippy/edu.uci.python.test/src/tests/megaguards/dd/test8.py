a = [[1, 2, 3], [1, 2, 3], [1, 2, 3]]
def t():
    for i in range(3):
        for j in range(3):
            a[j][i] = a[j][i]*2
t()
print(a)

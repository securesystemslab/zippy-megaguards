a = [[1, 2, 3], [1, 2, 3], [1, 2, 3]]
def t():
    for i in range(len(a)):
        for j in range(len(a)):
            a[i][j] = a[i][j]*2
t()
print(a)

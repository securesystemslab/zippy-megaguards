a = [1, 2, 3]
c = [0, 0, 0]
def t():
    for i in range(len(a)):
        c[i] = 1
        for j in range(len(a)):
            if(a[i] == 2):
                if(a[j] == 2):
                    break
                c[i] = a[i] * 3
            c[i] = a[i] * 2
        else:
            c[i] = 1
t()
print(c)

N = 5
def t():
    for i in range(N):
        for j in range(N):
            for k in range(N):
                a1 = j + k
                a2 = a1 * k
                a3 = a1 / (a2+1)
                a4 = a1 - a3
                a5 = a2 % (1+a1)
                a6 = a5 + a2 / (1 + a1 * a4)
t()

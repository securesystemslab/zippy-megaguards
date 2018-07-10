N=3

def t():
    a = [[[ i+1 for i in range(N)] for j in range(N) ] for k in range(N) ]
    c = [ 0 for i in range(N)]
    for i in range(N):
        for j in range(N):
            c[i] = a[0][0][i]*2
    return c


c = t()
c = t()
c = t()
print(c)

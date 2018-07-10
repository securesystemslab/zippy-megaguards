N=3

def t(x):
    a = [[[ i+j+k for i in range(N)] for j in range(N) ] for k in range(N) ]
    for i in range(3):
        for j in range(3):
            for k in range(x):
                c[i][j][k] += a[i][j][k]*2


c = [[[ 0 for i in range(N)] for j in range(N) ] for k in range(N) ]
t(2)
print(c)
c = [[[ 0 for i in range(N)] for j in range(N) ] for k in range(N) ]
t(3)
print(c)
c = [[[ 0 for i in range(N)] for j in range(N) ] for k in range(N) ]
t(2)
print(c)

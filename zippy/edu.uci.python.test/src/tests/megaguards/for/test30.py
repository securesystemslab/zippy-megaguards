a = [ i     for i in range(10000)]
b = [ i*2   for i in range(10000)]
c = [ 0     for i in range(10000)]
def t1():
    for j in range(1,len(a)-1, 2):
        for i in range(len(b)):
            c[j] = b[i] + a[j]*2

t1()
# print(c)

a = [ i + 0.    for i in range(10000)]
b = [ i*2 + 0.  for i in range(10000)]
c = [ 0.        for i in range(10000)]
def t2():
    for j in range(1,len(a)-1, 2):
        for i in range(len(b)):
            c[j] = b[i] + a[j]*2

t2()
# print(c)

a = [[ i    for i in range(100)] for j in range(100)]
b = [[ i*2  for i in range(100)] for j in range(100)]
c = [[ 0    for i in range(100)] for j in range(100)]
def t3():
    for j in range(1,len(a)-1, 2):
        for i in range(len(b)):
            c[j][i] = b[j][i] + a[i][j]*2

t3()
# print(c)

a = [[ i + 0.    for i in range(100)] for j in range(100)]
b = [[ i*2 + 0.  for i in range(100)] for j in range(100)]
c = [[ 0.        for i in range(100)] for j in range(100)]
def t4():
    for j in range(1,len(a[0])-1, 2):
        for i in range(len(b[0])):
            c[j][i] = b[j][i] + a[i][j]*2

t4()
# print(c)

a = [ i + 0.    for i in range(1000000)]
b = [ i*2 + 0.  for i in range(1000000)]
c = [ 0.        for i in range(1000000)]
def t5():
    for j in range(1,len(a)-1, 2):
        for i in range(len(b)):
            c[j] = b[i] + a[j]*2

t5()
# print(c)

a = [[ i + 0.    for i in range(1000)] for j in range(1000)]
b = [[ i*2 + 0.  for i in range(1000)] for j in range(1000)]
c = [[ 0.        for i in range(1000)] for j in range(1000)]
def t6():
    for j in range(1,len(a[0])-1, 2):
        for i in range(len(b[0])):
            c[j][i] = b[j][i] + a[i][j]*2

t6()
# print(c)

a = [[[ i + 0.    for i in range(100)] for j in range(100)] for k in range(100)]
b = [[[ i*2 + 0.  for i in range(100)] for j in range(100)] for k in range(100)]
c = [[[ 0.        for i in range(100)] for j in range(100)] for k in range(100)]
def t7():
    for j in range(1,len(a[0])-1, 2):
        for i in range(len(b[0])):
            c[j][i][0] = b[j][i][0] + a[i][j][0]*2

t7()
# print(c)

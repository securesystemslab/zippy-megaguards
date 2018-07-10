n=5
def foo(x, y):
    return x*2 - y + n

def bar(x, y):
    return x * y + n

def test(func, x, y):
    print(list(map(func, x, y)))

a = [i for i in range(10)]
b = [i for i in range(10)]

c = [i*2. for i in range(20)]
d = [i*3. for i in range(20)]

test(foo, a, b)
test(foo, a, b)
test(bar, a, b)
test(bar, c, d)

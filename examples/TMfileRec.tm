##TMfileFct.tm

def fib(a, b, N){
        print(a)
        if(N!=0){
            fib(b, a+b, N-1)
        }
}

fib(0, 1, 3)

def array_add (a){

    def add(a,b){
        return (add2(a, b))
        def add2 (a, b){
            return (a+b)
        }
    }

    if (a.length == 0){
        return (0)
    }
    let i = 1
    let result = a.get(0)

    while (i < a.length) {
        result = add(result, a.get(i))
        i = i + 1
    }
    return (result)
}

def isPrime(number) {
            if (number <= 1) {return (false)}
            let prime = true
            let i = 2
            while (i < number && prime) {
                if (number%i == 0) {prime = false}
            i = i + 1
            }
            return (prime)
}

def tricky2 (args) {
            let N = parseInt(args.get(0))
            let current = 2
            let count = 0
            while (count < N) {
                if (isPrime(current)) {
                print(current+aString)
                count = count + 1
                }
            current = current + 1
            }
}

def fizzbuzz(args) {
        let i = 1
        while (i <= 100) {
            if (i%15 == 0){
                print("FizzBuzz")
            } else {if (i % 3 == 0){
            print("Fizz")
            }
            else{ if (i % 5 == 0){
            print("Buzz")
            }
            else {
                print(i+aString)
            }}}
            i = i + 1
        }
        }

def swap(a, i, j) {
            let tmp = a.get(i)
            a.put(i:a.get(j))
            a.put(j:tmp)
        }

        def sort(numbers) {
            let i = 0
            while (i < numbers.length) {
                let j = i+1
                while (j < numbers.length) {
                    if (numbers.get(i) > numbers.get(j)){
                        swap(numbers, i, j)
                        j = j + 1
                    }
                i = i + 1
                }
            }
        }

        def man(args) {
            let numbers=arrayOf(anInt:args.length)
            let i = 0
            while (i < args.length) {
                numbers.put(parseInt(args.get(i)): i)
                i = i + 1
            }
            sort(numbers)
            i = 0
            while (i < numbers.length) {
                print(numbers.get(i)+aString)
                i = i + 1
            }
        }


    def sum(a,b){
        return (a+b)
    }

    let a=sum(5,10)
    let b=sum(5.5,9.5)
    let c=sum("additioner"," des")
    let d=sum(c," Strings")
    print(a)
    print(b)
    print(c)
    print(d)

    def equals (a, b) { return (a == b) }

    struct Pair {
        a=anInt
        b=anInt
    }

    let e = equals("text", "text")
    let g = equals(5, 5)
    let h = equals(new Pair(1,2), new Pair(1,2))

let total= array_add([1, 2, 3])
let float_total = total + 0.0

let math= 1 + 3 * 4 * (1 + 3) / 12

def validate5(value)
{
    if (math != 5){
        print("It's a bug! We wanted 5 but got: " + math)
    }
    if (math > 5){
        print("It was too big.")
    }else{ if (math < 5){
            print("It was too small.")
        }else{
            print("It's just right.")
        }
    }
}

validate5(math)
validate5(6)


let type = new Pair()
print(type + aString) 


def sum_pair (pair){
    return (pair.a + pair.b)
}

validate5(sum_pair( new Pair(2, 3)))

def use_array (array) {}

let intArray= arrayOf(anInt:0)
let stringArray= arrayOf(aString:0)
use_array(arrayOf(0:0))
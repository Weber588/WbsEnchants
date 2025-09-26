package wbs.enchants.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DataComponentMapProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Method invoked: " + method.getName());

        if (method.getName().equals("get")) {
            System.out.println("Performing some dynamic action.");
            return null; // void method
        } else if (method.getName().equals("getData")) {
            return "Dynamic Data"; // return a value
        }
        return method.invoke(proxy, args); // Delegate to the underlying object if available
    }
}
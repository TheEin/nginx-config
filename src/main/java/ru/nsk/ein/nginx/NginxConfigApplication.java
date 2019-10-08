package ru.nsk.ein.nginx;

import com.github.odiszapc.nginxparser.NgxConfig;
import com.github.odiszapc.nginxparser.NgxEntry;
import org.python.core.PyFunction;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Scanner;


public class NginxConfigApplication {

    private static PythonInterpreter py;

    /**
     * Usage: path/to/nginx.conf
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            System.exit(-1);
        }
        String path = args[0];
        NgxConfig conf = parseConfig(readContent(path));
        print(conf);
    }

    private static void print(NgxEntry e) {
        System.out.println(e.getClass() + ": " + e);
        if (e instanceof Iterable) {
            ((Iterable) e).forEach(o -> print((NgxEntry) o));
        }
    }

    private static NgxConfig parseConfig(String config) throws IOException {
        py = new PythonInterpreter();
        try (InputStream usage = ClassLoader.getSystemResourceAsStream("renderTemplate.py")) {
            exec(readContent(usage));
        }
        py.set("conf", new PyString(config));
        String res = py.eval("renderTemplate(conf, {})").asString();
        try (InputStream input = new ByteArrayInputStream(res.getBytes())) {
            return NgxConfig.read(input);
        }
    }

    private static void exec(String code) {
        System.out.println(code);
        py.exec(code);
    }

    private static String readContent(String path) throws IOException {
        try (InputStream file = new FileInputStream(path)) {
            return readContent(file);
        }
    }

    private static String readContent(InputStream inputStream) {
        return Optional.ofNullable(inputStream)
                .map(is -> new Scanner(is).useDelimiter("\\A"))
                .map(Scanner::next)
                .orElse(null);
    }

    private static void printUsage() throws Exception {
        try (InputStream usage = ClassLoader.getSystemResourceAsStream("usage.txt")) {
            System.out.println(readContent(usage));
        }
    }
}

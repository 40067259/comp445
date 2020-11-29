package com.concordia.udp;

import java.util.Scanner;

public class HttpcApplication {

    public static void main(String[] args) {
        ResponseAndPrint responseAndPrint = new ResponseAndPrint();
        System.out.println("Input something...");
        Scanner myObj = new Scanner(System.in);
        String userInput = myObj.nextLine();
        String[] inputArgs = userInput.split(" ");

        responseAndPrint.parse(inputArgs);

    }

}

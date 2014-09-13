package Kirrkirr.util;

/**
 *  Helper class to encode and decode filenames using URL encoding scheme
 *  similar to that provided in java's built in URLEncoder and URLDecoder
 *  classes (which unfortunately don't appear until the 1.3 release).
 *  Modeled closely after http://www.w3.org/International/URLUTF8Encoder.java
 *  and explanation of UTF-8 encoding on http://www1.tip.nl/~t876506/utf8tbl.html
 */

public class URLHelper extends Object {

    public static String encode(String toEncode) {
        StringBuffer buf = new StringBuffer();

        for(int i = 0; i < toEncode.length(); i++) {
            int ch = toEncode.charAt(i);

            //unaltered chars should just be appended
            if((ch >= 'A' && ch <='Z')||(ch >= 'a' && ch <='z')||
               (ch >='0' && ch <='9')||ch == '.'||ch =='-'||
               ch=='*'||ch=='_' || ch == '@') {
                buf.append((char) ch);
            }
            else if(ch == ' ') {
                buf.append('+');  //encoding of spaces
            }
            else {
                //need to encode using (possibly multiple) hex values

                if(ch<128) {
                    //append hex representation of ch
                    buf.append(decToHexString(ch));
                }
                else if(ch <2048) {
                    //2 bytes of hex
                    buf.append(decToHexString(192|ch>>6));      //divide by 64, add 192
                    buf.append(decToHexString(128|ch&63));      //mod by 64, add 128

                }
                else {
                    //3 bytes of hex
                    buf.append(decToHexString(224|ch>>12));     //divide by 4096, add 22
                    buf.append(decToHexString(128|(ch>>6)&63)); //divide by 64, mod
                                                     //64, add 128
                    buf.append(decToHexString(128|ch&63));      //mod 64, add 128
                }

            }

        }
        //Dbg.print("string: " + toEncode + " encoded as: " + buf.toString());
        return buf.toString();
    }

    public static String decode(String toDecode) {
        StringBuffer buf = new StringBuffer();
        String hex;

        for(int i = 0; i < toDecode.length(); i++) {
            int ch = toDecode.charAt(i);

            if (ch == '+') { //reverse encoding of space char
                buf.append(' ');
            } else if (ch == '%') {
                //reverse encoding of chars encoded as hex
                hex = toDecode.substring(i, i+3);
                ch = hexStringToDec(hex);

                if(ch < 128) {
                    //one byte of hex
                    buf.append((char) ch);
                }
                else if(ch >=192 && ch <224) {
                    //two bytes of hex
                    //first byte
                    ch = (ch-192) << 6;

                    //second byte
                    i+=3;
                    hex = toDecode.substring(i, i+3);
                    ch |= (hexStringToDec(hex) - 128);

                    buf.append((char) ch);
                }
                else if(ch >= 224) {
                    //three bytes of hex
                    //first byte
                    ch = (ch - 224) << 12;

                    //second byte
                    i+=3;
                    hex = toDecode.substring(i, i+3);
                    ch |= (hexStringToDec(hex) - 128) << 6;

                    //third byte
                    i+=3;
                    hex = toDecode.substring(i, i+3);
                    ch |= (hexStringToDec(hex) - 128);

                    buf.append((char) ch);
                }
                i+=2; //so i++ in for loop will work properly
            } else {
                //unaltered chars should just be appended
                buf.append((char) ch);
                // check if there are chars that should have been encoded
                if (Dbg.FILE) {
                    if ((ch >= 'A' && ch <='Z')||(ch >= 'a' && ch <='z')||
                        (ch >='0' && ch <='9')||ch == '.'||ch =='-'||
                        ch=='*'||ch=='_' || ch == '@') {
                        // okay
                    } else {
                        Dbg.print("URLHelper.decode: " + ch +
                                  " should have been encoded.");
                    }
                }
            }
        }
        if (Dbg.FILE) {
            Dbg.print("encoded string: " + toDecode + " decoded as: "
                      + buf.toString());
        }
        return buf.toString();
    }


    private static String decToHexString(int ch) {
        StringBuffer ret = new StringBuffer("%");
        int upper = ch >> 4;
        if(upper < 10) ret.append(upper); //append 0-9
        else ret.append((char) (55 + upper)); //append A-F
        int lower = ch & 15;
        if(lower < 10) ret.append(lower); //append 0-9
        else ret.append((char) (55 + lower)); //append A-F
        return ret.toString();
    }

    private static int hexStringToDec(String hex) {
        int upper = hex.charAt(1);
        int lower = hex.charAt(2);
        if(upper >= 'A')
            upper = (upper - 55 << 4);
        else
            upper = (upper - '0') << 4;
        if(lower >= 'A')
            lower = (lower - 55);
        else
            lower = (lower - '0');
        return upper | lower;
    }


    public static void main(String[] args) {
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                System.out.println(args[i] + " --> " + decode(args[i]));
            }
        } else {
            System.out.println("hello = " + decode(encode("hello")));
            System.out.println("hello there = " +
                               decode(encode("hello there")));
            System.out.println("hello/there = " +
                               decode(encode("hello/there")));
            System.out.println(" /% = " +
                               decode(encode(" /%")));
        }
    }

}


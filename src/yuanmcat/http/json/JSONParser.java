package yuanmcat.http.json;


import yuanmcat.http.json.parser.Parser;
import yuanmcat.http.json.tokenizer.CharReader;
import yuanmcat.http.json.tokenizer.TokenList;
import yuanmcat.http.json.tokenizer.Tokenizer;

import java.io.IOException;
import java.io.StringReader;


public class JSONParser {

    private Tokenizer tokenizer = new Tokenizer();

    private Parser parser = new Parser();

    public Object fromJSON(String json) throws IOException {
        CharReader charReader = new CharReader(new StringReader(json));
        TokenList tokens = tokenizer.tokenize(charReader);
        return parser.parse(tokens);
    }
}

package interpreter;

import gui.Customer_product_reg;
import gui.diliver_type;


public class RemarkInterpreter implements Expression {

    @Override
    public void interpret(Context context) {
        String remark = context.getInput().toLowerCase();

        if (remark.contains("extra chicken")) {
            diliver_type.chicken = true;
        } else {
            diliver_type.chicken = false;
        }

        if (remark.contains("extra cheese")) {
            diliver_type.cheese = true;
        } else {
            diliver_type.cheese = false;
        }

        if (remark.contains("extra tomato")) {
            diliver_type.tomato = true;
        } else {
            diliver_type.tomato = false;
        }

        if (remark.contains("extra mushroom")) {
            diliver_type.mushroom = true;
        } else {
            diliver_type.mushroom = false;
        }

        if (remark.contains("extra pork")) {
            diliver_type.pork = true;
        } else {
            diliver_type.pork = false;
        }

        if (remark.contains("extra ketchup")) {
            diliver_type.ketchup = true;
        } else {
            diliver_type.ketchup = false;
        }

        if (remark.contains("extra fish")) {
            diliver_type.fish = true;
        } else {
            diliver_type.fish = false;
        }
    }
}

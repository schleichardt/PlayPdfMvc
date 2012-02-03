package controllers;

import play.mvc.Controller;

import java.util.Date;

public class Application extends Controller {
    public static void index() {
        String title = "the title";
        String text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.";
        Date date = new Date();
        render(title, text, date);
    }
}
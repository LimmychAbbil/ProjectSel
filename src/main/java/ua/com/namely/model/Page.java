package ua.com.namely.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Page {

    private String location;
    private PageType pageType;
    private Lang language;

}

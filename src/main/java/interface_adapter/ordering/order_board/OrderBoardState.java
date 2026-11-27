package interface_adapter.ordering.order_board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** What the order board shows: display-ready tickets plus a status or error message. */
public final class OrderBoardState {
    private List<OrderTicketRow> tickets = Collections.emptyList();
    private String message = "";
    private boolean error;

    public List<OrderTicketRow> getTickets() { return tickets; }
    public String getMessage() { return message; }
    public boolean isError() { return error; }

    /** Replaces the tickets, keeping a read-only copy. */
    public void setTickets(List<OrderTicketRow> newTickets) {
        tickets = Collections.unmodifiableList(new ArrayList<>(newTickets));
    }

    /** Shows an informational message. */
    public void succeed(String newMessage) {
        message = newMessage == null ? "" : newMessage;
        error = false;
    }

    /** Shows an error message. The tickets are left as they were. */
    public void fail(String newMessage) {
        message = newMessage == null ? "" : newMessage;
        error = true;
    }
}

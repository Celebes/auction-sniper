package pl.kgurniak.auctionsniper.ui;

import pl.kgurniak.auctionsniper.SniperListener;
import pl.kgurniak.auctionsniper.SniperSnapshot;
import pl.kgurniak.auctionsniper.enums.SniperState;
import pl.kgurniak.auctionsniper.util.Defect;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SnipersTableModel extends AbstractTableModel implements SniperListener {
    private static String[] STATUS_TEXT = {"Joining", "Bidding", "Winning", "Lost", "Won"};
    private List<SniperSnapshot> snapshots = new ArrayList<>();

    @Override
    public void sniperStateChanged(SniperSnapshot newSniperSnapshot) {
        int row = rowMatching(newSniperSnapshot);
        snapshots.set(row, newSniperSnapshot);
        fireTableRowsUpdated(row, row);
    }

    public void addSniper(SniperSnapshot snapshot) {
        int row = snapshots.size();
        snapshots.add(snapshot);
        fireTableRowsInserted(row, row);
    }

    public int getColumnCount() {
        return Column.values().length;
    }

    public int getRowCount() {
        return snapshots.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return Column.at(columnIndex).valueIn(snapshots.get(rowIndex));
    }

    public static String textFor(SniperState state) {
        return STATUS_TEXT[state.ordinal()];
    }

    private int rowMatching(SniperSnapshot newSnapshot) {
        for (int i = 0; i < snapshots.size(); i++) {
            if (newSnapshot.isForSameItemAs(snapshots.get(i))) {
                return i;
            }
        }
        throw new Defect("Cannot find match for " + newSnapshot);
    }

    @Override
    public String getColumnName(int column) {
        return Column.at(column).name;
    }
}

import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

public class MatrixMultiplication {

    static final int SIZE = 100;

    static int[][] matrixA = new int[SIZE][SIZE];
    static int[][] matrixB = new int[SIZE][SIZE];
    static long[][] result = new long[SIZE][SIZE];

    static AtomicInteger multiplicationCount =
            new AtomicInteger(0);

    static AtomicInteger completedCells =
            new AtomicInteger(0);

    static volatile int activeRow = -1;
    static volatile int activeColumn = -1;
    static volatile boolean finished = false;

    static MatrixAnimation animation;


    // =========================================================
    // INITIALIZE MATRICES
    // =========================================================

    static void initializeMatrices() {

        for (int i = 0; i < SIZE; i++) {

            for (int j = 0; j < SIZE; j++) {

                matrixA[i][j] =
                        (i + 2 * j) % 9 + 1;

                matrixB[i][j] =
                        (2 * i + j) % 9 + 1;
            }
        }
    }


    // =========================================================
    // MATRIX MULTIPLICATION USING VIRTUAL THREADS
    // =========================================================

    static void multiplyWithThreads() {

        for (int i = 0; i < SIZE; i++) {

            for (int j = 0; j < SIZE; j++) {

                activeRow = i;
                activeColumn = j;

                Thread[] workers =
                        new Thread[SIZE];

                Object cellLock =
                        new Object();

                for (int k = 0; k < SIZE; k++) {

                    final int row = i;
                    final int column = j;
                    final int position = k;

                    /*
                     * One Virtual Thread is created
                     * for every scalar multiplication.
                     */
                    workers[k] =
                            Thread.startVirtualThread(() -> {

                                long product =
                                        (long) matrixA[row][position]
                                        * matrixB[position][column];

                                synchronized (cellLock) {

                                    result[row][column]
                                            += product;
                                }

                                multiplicationCount
                                        .incrementAndGet();
                            });
                }


                // Wait for all 100 threads of this cell
                for (Thread worker : workers) {

                    try {

                        worker.join();

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                        return;
                    }
                }


                completedCells.incrementAndGet();


                /*
                 * Small delay so that the animation
                 * can clearly show the matrix being built.
                 */
                try {

                    Thread.sleep(3);

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }


        activeRow = -1;
        activeColumn = -1;
        finished = true;
    }


    // =========================================================
    // TENSORFLOW VERIFICATION
    // =========================================================

    static long[][] tensorflowCalculation() {

        try (Graph graph = new Graph()) {

            TFloat32 tensorA =
                    TFloat32.tensorOf(
                            Shape.of(SIZE, SIZE)
                    );

            TFloat32 tensorB =
                    TFloat32.tensorOf(
                            Shape.of(SIZE, SIZE)
                    );


            for (int i = 0; i < SIZE; i++) {

                for (int j = 0; j < SIZE; j++) {

                    tensorA.setFloat(
                            matrixA[i][j],
                            i,
                            j
                    );

                    tensorB.setFloat(
                            matrixB[i][j],
                            i,
                            j
                    );
                }
            }


            org.tensorflow.op.Ops tf =
                    org.tensorflow.op.Ops.create(graph);


            var inputA =
                    tf.constant(tensorA);

            var inputB =
                    tf.constant(tensorB);


            var multiplication =
                    tf.linalg.matMul(
                            inputA,
                            inputB
                    );


            try (Session session =
                         new Session(graph)) {

                Tensor output =
                        session.runner()
                                .fetch(multiplication)
                                .run()
                                .get(0);


                TFloat32 outputData =
                        (TFloat32) output;


                long[][] tensorflowResult =
                        new long[SIZE][SIZE];


                for (int i = 0; i < SIZE; i++) {

                    for (int j = 0; j < SIZE; j++) {

                        tensorflowResult[i][j] =
                                Math.round(
                                        outputData.getFloat(
                                                i,
                                                j
                                        )
                                );
                    }
                }


                output.close();
                tensorA.close();
                tensorB.close();


                return tensorflowResult;
            }
        }
    }


    // =========================================================
    // COMPARE RESULTS
    // =========================================================

    static boolean compareResults(
            long[][] tensorflowResult) {

        for (int i = 0; i < SIZE; i++) {

            for (int j = 0; j < SIZE; j++) {

                if (result[i][j]
                        != tensorflowResult[i][j]) {

                    return false;
                }
            }
        }

        return true;
    }


    // =========================================================
    // DISPLAY RESULT
    // =========================================================

    static void displayResult() {

        System.out.println();

        System.out.println(
                "First 5 x 5 elements of Result Matrix"
        );

        System.out.println(
                "---------------------------------------"
        );


        for (int i = 0; i < 5; i++) {

            for (int j = 0; j < 5; j++) {

                System.out.printf(
                        "%8d",
                        result[i][j]
                );
            }

            System.out.println();
        }
    }


    // =========================================================
    // MATRIX GRID PANEL
    // =========================================================

    static class GridPanel extends JPanel {

        BufferedImage image;

        String title;

        int type;


        /*
         * type:
         * 0 = Matrix A
         * 1 = Matrix B
         * 2 = Matrix C
         */

        GridPanel(
                String title,
                int type) {

            this.title = title;
            this.type = type;

            setPreferredSize(
                    new Dimension(
                            300,
                            300
                    )
            );

            image =
                    new BufferedImage(
                            SIZE,
                            SIZE,
                            BufferedImage.TYPE_INT_RGB
                    );

            setBorder(
                    BorderFactory.createLineBorder(
                            Color.LIGHT_GRAY,
                            1
                    )
            );
        }


        void updateImage() {

            Graphics2D g =
                    image.createGraphics();


            for (int i = 0; i < SIZE; i++) {

                for (int j = 0; j < SIZE; j++) {

                    if (type == 0) {

                        /*
                         * Matrix A - blue shades
                         */
                        int value =
                                matrixA[i][j];

                        g.setColor(
                                new Color(
                                        170 - value * 8,
                                        205 - value * 6,
                                        255
                                )
                        );

                    } else if (type == 1) {

                        /*
                         * Matrix B - green shades
                         */
                        int value =
                                matrixB[i][j];

                        g.setColor(
                                new Color(
                                        175 - value * 5,
                                        245 - value * 5,
                                        175 - value * 3
                                )
                        );

                    } else {

                        /*
                         * Matrix C - building live
                         */

                        int position =
                                i * SIZE + j;

                        int completed =
                                completedCells.get();


                        if (position < completed) {

                            long value =
                                    result[i][j];

                            int shade =
                                    (int) Math.abs(
                                            value % 100
                                    );


                            g.setColor(
                                    new Color(
                                            210 + shade / 5,
                                            120 + shade / 4,
                                            145 + shade / 5
                                    )
                            );

                        } else {

                            /*
                             * Not calculated yet
                             */
                            g.setColor(
                                    new Color(
                                            245,
                                            220,
                                            225
                                    )
                            );
                        }
                    }


                    g.fillRect(
                            j,
                            i,
                            1,
                            1
                    );
                }
            }


            g.dispose();

            repaint();
        }


        @Override
        protected void paintComponent(
                Graphics g) {

            super.paintComponent(g);


            /*
             * Scale the 100x100 matrix
             * to a 300x300 display.
             */
            g.drawImage(
                    image,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    null
            );


            /*
             * Highlight currently processed cell
             * in Matrix C.
             */
            if (type == 2
                    && !finished
                    && activeRow >= 0
                    && activeColumn >= 0) {

                int x =
                        activeColumn
                        * getWidth()
                        / SIZE;

                int y =
                        activeRow
                        * getHeight()
                        / SIZE;


                int cellWidth =
                        getWidth() / SIZE;

                int cellHeight =
                        getHeight() / SIZE;


                Graphics2D g2 =
                        (Graphics2D) g;


                g2.setColor(
                        Color.RED
                );

                g2.setStroke(
                        new BasicStroke(2)
                );


                g2.drawRect(
                        x,
                        y,
                        cellWidth,
                        cellHeight
                );
            }
        }
    }


    // =========================================================
    // ANIMATION WINDOW
    // =========================================================

    static class MatrixAnimation
            extends JFrame {

        GridPanel panelA;
        GridPanel panelB;
        GridPanel panelC;


        JLabel processingLabel;
        JLabel operationLabel;
        JLabel cellLabel;
        JLabel statusLabel;

        JProgressBar cellProgress;
        JProgressBar operationProgress;


        MatrixAnimation() {

            setTitle(
                    "Matrix Multiplication in Action (100 x 100)"
            );


            setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );


            setLayout(
                    new BorderLayout(
                            10,
                            10
                    )
            );


            // -------------------------------------------------
            // TITLE
            // -------------------------------------------------

            JLabel title =
                    new JLabel(
                            "Matrix Multiplication Using Threads",
                            SwingConstants.CENTER
                    );


            title.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            22
                    )
            );


            add(
                    title,
                    BorderLayout.NORTH
            );


            // -------------------------------------------------
            // THREE MATRICES
            // -------------------------------------------------

            JPanel matrices =
                    new JPanel(
                            new GridLayout(
                                    1,
                                    3,
                                    25,
                                    5
                            )
                    );


            panelA =
                    new GridPanel(
                            "Matrix A",
                            0
                    );


            panelB =
                    new GridPanel(
                            "Matrix B",
                            1
                    );


            panelC =
                    new GridPanel(
                            "Matrix C (building live)",
                            2
                    );


            matrices.add(
                    createMatrixContainer(
                            panelA,
                            "Matrix A"
                    )
            );


            matrices.add(
                    createMatrixContainer(
                            panelB,
                            "Matrix B"
                    )
            );


            matrices.add(
                    createMatrixContainer(
                            panelC,
                            "Matrix C (building live)"
                    )
            );


            add(
                    matrices,
                    BorderLayout.CENTER
            );


            // -------------------------------------------------
            // INFORMATION AREA
            // -------------------------------------------------

            JPanel bottom =
                    new JPanel();


            bottom.setLayout(
                    new BoxLayout(
                            bottom,
                            BoxLayout.Y_AXIS
                    )
            );


            processingLabel =
                    new JLabel(
                            "Processing row: 0    column: 0"
                    );


            processingLabel.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            15
                    )
            );


            cellLabel =
                    new JLabel(
                            "Completed cells: 0 / 10000"
                    );


            operationLabel =
                    new JLabel(
                            "Multiplications: 0 / 1000000"
                    );


            statusLabel =
                    new JLabel(
                            "Status: Processing..."
                    );


            cellProgress =
                    new JProgressBar(
                            0,
                            10000
                    );


            cellProgress.setStringPainted(
                    true
            );


            operationProgress =
                    new JProgressBar(
                            0,
                            1000000
                    );


            operationProgress.setStringPainted(
                    true
            );


            bottom.add(
                    processingLabel
            );

            bottom.add(
                    cellLabel
            );

            bottom.add(
                    cellProgress
            );

            bottom.add(
                    operationLabel
            );

            bottom.add(
                    operationProgress
            );

            bottom.add(
                    statusLabel
            );


            add(
                    bottom,
                    BorderLayout.SOUTH
            );


            // -------------------------------------------------
            // ANIMATION TIMER
            // -------------------------------------------------

            Timer timer =
                    new Timer(
                            80,
                            e -> updateAnimation()
                    );


            timer.start();


            pack();


            setSize(
                    1100,
                    720
            );


            setLocationRelativeTo(
                    null
            );


            setVisible(
                    true
            );


            updateAnimation();
        }


        JPanel createMatrixContainer(
                GridPanel panel,
                String text) {

            JPanel container =
                    new JPanel(
                            new BorderLayout()
                    );


            JLabel label =
                    new JLabel(
                            text,
                            SwingConstants.CENTER
                    );


            label.setFont(
                    new Font(
                            "Arial",
                            Font.BOLD,
                            18
                    )
            );


            container.add(
                    label,
                    BorderLayout.NORTH
            );


            container.add(
                    panel,
                    BorderLayout.CENTER
            );


            return container;
        }


        void updateAnimation() {

            panelA.updateImage();

            panelB.updateImage();

            panelC.updateImage();


            int completed =
                    completedCells.get();

            int operations =
                    multiplicationCount.get();


            cellProgress.setValue(
                    completed
            );


            operationProgress.setValue(
                    operations
            );


            cellLabel.setText(
                    "Completed cells: "
                    + completed
                    + " / 10000"
            );


            operationLabel.setText(
                    "Multiplications: "
                    + operations
                    + " / 1000000"
            );


            if (activeRow >= 0) {

                processingLabel.setText(
                        "Processing row: "
                        + activeRow
                        + "    column: "
                        + activeColumn
                );

            } else {

                processingLabel.setText(
                        "Processing row: Completed    column: Completed"
                );
            }


            if (finished) {

                statusLabel.setText(
                        "Status: Calculation Completed"
                );
            }
        }
    }


    // =========================================================
    // MAIN
    // =========================================================

    public static void main(
            String[] args) {

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "       MATRIX MULTIPLICATION USING THREADS"
        );

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "Matrix A              : 100 x 100"
        );

        System.out.println(
                "Matrix B              : 100 x 100"
        );

        System.out.println(
                "Result Matrix         : 100 x 100"
        );

        System.out.println(
                "Thread Technology     : Java Virtual Threads"
        );

        System.out.println(
                "Framework             : TensorFlow"
        );

        System.out.println(
                "Total Multiplications : 1,000,000"
        );


        // Initialize matrices
        initializeMatrices();


        // Start animation
        SwingUtilities.invokeLater(
                () -> animation =
                        new MatrixAnimation()
        );


        try {

            Thread.sleep(500);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
            return;
        }


        // Start timing
        long startTime =
                System.nanoTime();


        // Perform multiplication
        multiplyWithThreads();


        // End timing
        long endTime =
                System.nanoTime();


        double executionTime =
                (endTime - startTime)
                / 1_000_000_000.0;


        System.out.println();

        System.out.println(
                "Thread-based multiplication completed."
        );


        System.out.println(
                "Completed operations: "
                + multiplicationCount.get()
        );


        System.out.println(
                "Completed cells: "
                + completedCells.get()
        );


        System.out.printf(
                "Execution time: %.3f seconds%n",
                executionTime
        );


        // TensorFlow verification

        System.out.println();

        System.out.println(
                "Verifying result using TensorFlow..."
        );


        long[][] tensorflowResult =
                tensorflowCalculation();


        boolean correct =
                compareResults(
                        tensorflowResult
                );


        System.out.println(
                "TensorFlow verification: "
                + (
                        correct
                                ? "PASSED"
                                : "FAILED"
                )
        );


        displayResult();


        System.out.println();

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "             PROGRAM COMPLETED"
        );

        System.out.println(
                "=============================================="
        );
    }
}
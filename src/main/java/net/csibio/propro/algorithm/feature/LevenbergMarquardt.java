package net.csibio.propro.algorithm.feature;

import lombok.Data;
import net.finmath.functions.LinearAlgebra;
import net.finmath.optimizer.OptimizerInterface;
import net.finmath.optimizer.SolverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;


/**
 * Created by Nico Wang
 * Time: 2018-09-17 19:47
 */

@Data
public abstract class LevenbergMarquardt implements Serializable, Cloneable, OptimizerInterface {

    public static final Logger logger = LoggerFactory.getLogger(LevenbergMarquardt.class);

    private static final long serialVersionUID = 4560864869394838155L;
    private final net.finmath.optimizer.LevenbergMarquardt.RegularizationMethod regularizationMethod;
    private double[] initialParameters;
    private double[] parameterSteps;
    private double[] targetValues;
    private double[] weights;
    private int maxIteration;
    private double lambda;
    private double lambdaDivisor;
    private double lambdaMultiplicator;
    private double errorRootMeanSquaredTolerance;
    private int iteration;
    private double[] parameterTest;
    private double[] parameterIncrement;
    private double[] valueTest;
    private double[] parameterCurrent;
    private double[] valueCurrent;
    private double[][] derivativeCurrent;
    private double errorMeanSquaredCurrent;
    private double errorRootMeanSquaredChange;
    private boolean isParameterCurrentDerivativeValid;
    private double[][] hessianMatrix;
    private double[] beta;
    private int numberOfThreads;
    private ExecutorService executor;
    private boolean executorShutdownWhenDone;

    public static void main(String[] args) throws SolverException, CloneNotSupportedException {
        LevenbergMarquardt optimizer = new LevenbergMarquardt() {
            private static final long serialVersionUID = -282626938650139518L;

            @Override
            public void setValues(double[] parameters, double[] values) {
                values[0] = parameters[0] * 0.0D + parameters[1];
                values[1] = parameters[0] * 2.0D + parameters[1];
            }
        };
        optimizer.setInitialParameters(new double[]{0.0D, 0.0D});
        optimizer.setWeights(new double[]{1.0D, 1.0D});
        optimizer.setMaxIteration(100);
        optimizer.setTargetValues(new double[]{5.0D, 10.0D});
        optimizer.run();
        double[] bestParameters = optimizer.getBestFitParameters();
        logger.info("The solver for problem 1 required " + optimizer.getIterations() + " iterations. The best fit parameters are:");

        for (int i = 0; i < bestParameters.length; ++i) {
            logger.info("\tparameter[" + i + "]: " + bestParameters[i]);
        }

        OptimizerInterface optimizer2 = optimizer.getCloneWithModifiedTargetValues(new double[]{5.1D, 10.2D}, new double[]{1.0D, 1.0D}, true);
        optimizer2.run();
        double[] bestParameters2 = optimizer2.getBestFitParameters();
        logger.info("The solver for problem 2 required " + optimizer2.getIterations() + " iterations. The best fit parameters are:");

        for (int i = 0; i < bestParameters2.length; ++i) {
            logger.info("\tparameter[" + i + "]: " + bestParameters2[i]);
        }

    }

    public LevenbergMarquardt(net.finmath.optimizer.LevenbergMarquardt.RegularizationMethod regularizationMethod, double[] initialParameters, double[] targetValues, int maxIteration, ExecutorService executorService) {
        this.initialParameters = null;
        this.parameterSteps = null;
        this.targetValues = null;
        this.weights = null;
        this.maxIteration = 100;
        this.lambda = 0.001D;
        this.lambdaDivisor = 3.0D;
        this.lambdaMultiplicator = 2.0D;
        this.errorRootMeanSquaredTolerance = 0.0D;
        this.iteration = 0;
        this.parameterTest = null;
        this.parameterIncrement = null;
        this.valueTest = null;
        this.parameterCurrent = null;
        this.valueCurrent = null;
        this.derivativeCurrent = (double[][]) null;
        this.errorMeanSquaredCurrent = 1.0D / 0.0;
        this.errorRootMeanSquaredChange = 1.0D / 0.0;
        this.isParameterCurrentDerivativeValid = false;
        this.hessianMatrix = (double[][]) null;
        this.beta = null;
        this.numberOfThreads = 1;
        this.executor = null;
        this.executorShutdownWhenDone = true;
        this.regularizationMethod = regularizationMethod;
        this.initialParameters = initialParameters;
        this.targetValues = targetValues;
        this.maxIteration = maxIteration;
        this.weights = new double[targetValues.length];
        Arrays.fill(this.weights, 1.0D);
        this.executor = executorService;
        this.executorShutdownWhenDone = executorService == null;
        this.numberOfThreads = 1;
    }

    public LevenbergMarquardt(double[] initialParameters, double[] targetValues, int maxIteration, ExecutorService executorService) {
        this(net.finmath.optimizer.LevenbergMarquardt.RegularizationMethod.LEVENBERG_MARQUARDT, initialParameters, targetValues, maxIteration, executorService);
    }

    public LevenbergMarquardt(double[] initialParameters, double[] targetValues, int maxIteration, int numberOfThreads) {
        this((double[]) initialParameters, (double[]) targetValues, maxIteration, (ExecutorService) null);
        this.numberOfThreads = numberOfThreads;
    }

    public LevenbergMarquardt(List<Number> initialParameters, List<Number> targetValues, int maxIteration, ExecutorService executorService) {
        this(numberListToDoubleArray(initialParameters), numberListToDoubleArray(targetValues), maxIteration, executorService);
    }

    public LevenbergMarquardt(List<Number> initialParameters, List<Number> targetValues, int maxIteration, int numberOfThreads) {
        this((List) initialParameters, (List) targetValues, maxIteration, (ExecutorService) null);
        this.numberOfThreads = numberOfThreads;
    }

    public LevenbergMarquardt() {
        this.initialParameters = null;
        this.parameterSteps = null;
        this.targetValues = null;
        this.weights = null;
        this.maxIteration = 100;
        this.lambda = 0.001D;
        this.lambdaDivisor = 3.0D;
        this.lambdaMultiplicator = 2.0D;
        this.errorRootMeanSquaredTolerance = 0.0D;
        this.iteration = 0;
        this.parameterTest = null;
        this.parameterIncrement = null;
        this.valueTest = null;
        this.parameterCurrent = null;
        this.valueCurrent = null;
        this.derivativeCurrent = (double[][]) null;
        this.errorMeanSquaredCurrent = 1.0D / 0.0;
        this.errorRootMeanSquaredChange = 1.0D / 0.0;
        this.isParameterCurrentDerivativeValid = false;
        this.hessianMatrix = (double[][]) null;
        this.beta = null;
        this.numberOfThreads = 1;
        this.executor = null;
        this.executorShutdownWhenDone = true;
        this.regularizationMethod = net.finmath.optimizer.LevenbergMarquardt.RegularizationMethod.LEVENBERG_MARQUARDT;
    }

    private static double[] numberListToDoubleArray(List<Number> listOfNumbers) {
        double[] arrayOfDoubles = new double[listOfNumbers.size()];

        for (int i = 0; i < arrayOfDoubles.length; ++i) {
            arrayOfDoubles[i] = ((Number) listOfNumbers.get(i)).doubleValue();
        }

        return arrayOfDoubles;
    }

    public LevenbergMarquardt(int numberOfThreads) {
        this.initialParameters = null;
        this.parameterSteps = null;
        this.targetValues = null;
        this.weights = null;
        this.maxIteration = 100;
        this.lambda = 0.001D;
        this.lambdaDivisor = 3.0D;
        this.lambdaMultiplicator = 2.0D;
        this.errorRootMeanSquaredTolerance = 0.0D;
        this.iteration = 0;
        this.parameterTest = null;
        this.parameterIncrement = null;
        this.valueTest = null;
        this.parameterCurrent = null;
        this.valueCurrent = null;
        this.derivativeCurrent = (double[][]) null;
        this.errorMeanSquaredCurrent = 1.0D / 0.0;
        this.errorRootMeanSquaredChange = 1.0D / 0.0;
        this.isParameterCurrentDerivativeValid = false;
        this.hessianMatrix = (double[][]) null;
        this.beta = null;
        this.numberOfThreads = 1;
        this.executor = null;
        this.executorShutdownWhenDone = true;
        this.regularizationMethod = net.finmath.optimizer.LevenbergMarquardt.RegularizationMethod.LEVENBERG_MARQUARDT;
        this.numberOfThreads = numberOfThreads;
    }

    public LevenbergMarquardt setInitialParameters(double[] initialParameters) {
        if (this.done()) {
            throw new UnsupportedOperationException("Solver cannot be modified after it has run.");
        } else {
            this.initialParameters = initialParameters;
            return this;
        }
    }

    public LevenbergMarquardt setParameterSteps(double[] parameterSteps) {
        if (this.done()) {
            throw new UnsupportedOperationException("Solver cannot be modified after it has run.");
        } else {
            this.parameterSteps = parameterSteps;
            return this;
        }
    }

    public LevenbergMarquardt setTargetValues(double[] targetValues) {
        if (this.done()) {
            throw new UnsupportedOperationException("Solver cannot be modified after it has run.");
        } else {
            this.targetValues = targetValues;
            return this;
        }
    }

    public LevenbergMarquardt setMaxIteration(int maxIteration) {
        if (this.done()) {
            throw new UnsupportedOperationException("Solver cannot be modified after it has run.");
        } else {
            this.maxIteration = maxIteration;
            return this;
        }
    }

    public LevenbergMarquardt setWeights(double[] weights) {
        if (this.done()) {
            throw new UnsupportedOperationException("Solver cannot be modified after it has run.");
        } else {
            this.weights = weights;
            return this;
        }
    }

    public LevenbergMarquardt setErrorTolerance(double errorTolerance) {
        if (this.done()) {
            throw new UnsupportedOperationException("Solver cannot be modified after it has run.");
        } else {
            this.errorRootMeanSquaredTolerance = errorTolerance;
            return this;
        }
    }

    public double getLambda() {
        return this.lambda;
    }

    public void setLambda(double lambda) {
        this.lambda = lambda;
    }

    public double getLambdaMultiplicator() {
        return this.lambdaMultiplicator;
    }

    public void setLambdaMultiplicator(double lambdaMultiplicator) {
        if (lambdaMultiplicator <= 1.0D) {
            throw new IllegalArgumentException("Parameter lambdaMultiplicator is required to be > 1.");
        } else {
            this.lambdaMultiplicator = lambdaMultiplicator;
        }
    }

    public double getLambdaDivisor() {
        return this.lambdaDivisor;
    }

    public void setLambdaDivisor(double lambdaDivisor) {
        if (lambdaDivisor <= 1.0D) {
            throw new IllegalArgumentException("Parameter lambdaDivisor is required to be > 1.");
        } else {
            this.lambdaDivisor = lambdaDivisor;
        }
    }

    @Override
    public double[] getBestFitParameters() {
        return this.parameterCurrent;
    }

    @Override
    public double getRootMeanSquaredError() {
        return Math.sqrt(this.errorMeanSquaredCurrent);
    }

    public void setErrorMeanSquaredCurrent(double errorMeanSquaredCurrent) {
        this.errorMeanSquaredCurrent = errorMeanSquaredCurrent;
    }

    @Override
    public int getIterations() {
        return this.iteration;
    }

    public abstract void setValues(double[] var1, double[] var2) throws SolverException;

    public void setDerivatives(double[] parameters, double[][] derivatives) throws SolverException {
        Vector<Future<double[]>> valueFutures = new Vector(this.parameterCurrent.length);

        int parameterIndex;
        for(parameterIndex = 0; parameterIndex < this.parameterCurrent.length; ++parameterIndex) {
            final double[] parametersNew = (double[])parameters.clone();
            final double[] derivative = derivatives[parameterIndex];
            final int parameterIndexx = parameterIndex;
            Callable<double[]> worker = new Callable<double[]>() {
                @Override
                public double[] call() {
                    double parameterFiniteDifference;
                    if (LevenbergMarquardt.this.parameterSteps != null) {
                        parameterFiniteDifference = LevenbergMarquardt.this.parameterSteps[parameterIndexx];
                    } else {
                        parameterFiniteDifference = (Math.abs(parametersNew[parameterIndexx]) + 1.0D) * 1.0E-8D;
                    }

                    parametersNew[parameterIndexx] += parameterFiniteDifference;

                    try {
                        LevenbergMarquardt.this.setValues(parametersNew, derivative);
                    } catch (Exception var4) {
                        Arrays.fill(derivative, 0.0D / 0.0);
                    }

                    for(int valueIndex = 0; valueIndex < LevenbergMarquardt.this.valueCurrent.length; ++valueIndex) {
                        derivative[valueIndex] -= LevenbergMarquardt.this.valueCurrent[valueIndex];
                        derivative[valueIndex] /= parameterFiniteDifference;
                        if (Double.isNaN(derivative[valueIndex])) {
                            derivative[valueIndex] = 0.0D;
                        }
                    }

                    return derivative;
                }
            };
            if (this.executor != null) {
                Future<double[]> valueFuture = this.executor.submit(worker);
                valueFutures.add(parameterIndex, valueFuture);
            } else {
                FutureTask<double[]> valueFutureTask = new FutureTask(worker);
                valueFutureTask.run();
                valueFutures.add(parameterIndex, valueFutureTask);
            }
        }

        for(parameterIndex = 0; parameterIndex < this.parameterCurrent.length; ++parameterIndex) {
            try {
                derivatives[parameterIndex] = (double[])((Future)valueFutures.get(parameterIndex)).get();
            } catch (InterruptedException var10) {
                throw new SolverException(var10);
            } catch (ExecutionException var11) {
                throw new SolverException(var11);
            }
        }

    }

    boolean done() {
        return this.iteration > this.maxIteration || this.errorRootMeanSquaredChange <= this.errorRootMeanSquaredTolerance || Double.isInfinite(this.lambda);
    }

    @Override
    public void run() throws SolverException {
        if (this.numberOfThreads > 1 && this.executor == null) {
            this.executor = Executors.newFixedThreadPool(this.numberOfThreads);
            this.executorShutdownWhenDone = true;
        }

        try {
            int numberOfParameters = this.initialParameters.length;
            int numberOfValues = this.targetValues.length;
            this.parameterTest = (double[]) this.initialParameters.clone();
            this.parameterIncrement = new double[numberOfParameters];
            this.parameterCurrent = new double[numberOfParameters];
            this.valueTest = new double[numberOfValues];
            this.valueCurrent = new double[numberOfValues];
            this.derivativeCurrent = new double[this.parameterCurrent.length][this.valueCurrent.length];
            this.hessianMatrix = new double[this.parameterCurrent.length][this.parameterCurrent.length];
            this.beta = new double[this.parameterCurrent.length];
            this.iteration = 0;

            while (true) {
                ++this.iteration;
                this.setValues(this.parameterTest, this.valueTest);
                double errorMeanSquaredTest = this.getMeanSquaredError(this.valueTest);
                if (errorMeanSquaredTest < this.errorMeanSquaredCurrent) {
                    this.errorRootMeanSquaredChange = Math.sqrt(this.errorMeanSquaredCurrent) - Math.sqrt(errorMeanSquaredTest);
                    System.arraycopy(this.parameterTest, 0, this.parameterCurrent, 0, this.parameterCurrent.length);
                    System.arraycopy(this.valueTest, 0, this.valueCurrent, 0, this.valueCurrent.length);
                    this.errorMeanSquaredCurrent = errorMeanSquaredTest;
                    this.isParameterCurrentDerivativeValid = false;
                    this.lambda /= this.lambdaDivisor;
                } else {
                    this.errorRootMeanSquaredChange = Math.sqrt(errorMeanSquaredTest) - Math.sqrt(this.errorMeanSquaredCurrent);
                    this.lambda *= this.lambdaMultiplicator;
                }

                if (this.done()) {
                    return;
                }

                this.updateParameterTest();

                String logString = "Iteration: " + this.iteration + "\tLambda=" + this.lambda + "\tError Current:" + this.errorMeanSquaredCurrent + "\tError Change:" + this.errorRootMeanSquaredChange + "\t";
                for (int i = 0; i < this.parameterCurrent.length; ++i) {
                    logString = logString + "[" + i + "] = " + this.parameterCurrent[i] + "\t";
                }
                logger.info(logString);

            }
        } finally {
            if (this.executor != null && this.executorShutdownWhenDone) {
                this.executor.shutdown();
                this.executor = null;
            }

        }
    }

    public double getMeanSquaredError(double[] value) {
        double error = 0.0D;

        for (int valueIndex = 0; valueIndex < value.length; ++valueIndex) {
            double deviation = value[valueIndex] - this.targetValues[valueIndex];
            error += this.weights[valueIndex] * deviation * deviation;
        }

        return error / (double) value.length;
    }

    private void updateParameterTest() throws SolverException {
        if (!this.isParameterCurrentDerivativeValid) {
            this.setDerivatives(this.parameterCurrent, this.derivativeCurrent);
            this.isParameterCurrentDerivativeValid = true;
        }

        boolean hessianInvalid = true;

        int i;
        while (hessianInvalid) {
            hessianInvalid = false;

            int k;
            for (i = 0; i < this.parameterCurrent.length; ++i) {
                for (int j = i; j < this.parameterCurrent.length; ++j) {
                    double alphaElement = 0.0D;

                    for (k = 0; k < this.valueCurrent.length; ++k) {
                        alphaElement += this.weights[k] * this.derivativeCurrent[i][k] * this.derivativeCurrent[j][k];
                    }

                    if (i == j) {
                        if (this.regularizationMethod != net.finmath.optimizer.LevenbergMarquardt.RegularizationMethod.LEVENBERG) {
                            alphaElement += this.lambda;
                        } else if (alphaElement == 0.0D) {
                            alphaElement = this.lambda;
                        } else {
                            alphaElement *= 1.0D + this.lambda;
                        }
                    }

                    this.hessianMatrix[i][j] = alphaElement;
                    this.hessianMatrix[j][i] = alphaElement;
                }
            }

            for (i = 0; i < this.parameterCurrent.length; ++i) {
                double betaElement = 0.0D;
                double[] derivativeCurrentSingleParam = this.derivativeCurrent[i];

                for (k = 0; k < this.valueCurrent.length; ++k) {
                    betaElement += this.weights[k] * (this.targetValues[k] - this.valueCurrent[k]) * derivativeCurrentSingleParam[k];
                }

                this.beta[i] = betaElement;
            }

            try {
                this.parameterIncrement = LinearAlgebra.solveLinearEquationSymmetric(this.hessianMatrix, this.beta);
            } catch (Exception var7) {
                hessianInvalid = true;
                this.lambda *= 16.0D;
            }
        }

        for (i = 0; i < this.parameterCurrent.length; ++i) {
            this.parameterTest[i] = this.parameterCurrent[i] + this.parameterIncrement[i];
        }

    }

    @Override
    public LevenbergMarquardt clone() throws CloneNotSupportedException {
        LevenbergMarquardt clonedOptimizer = (LevenbergMarquardt) super.clone();
        clonedOptimizer.isParameterCurrentDerivativeValid = false;
        clonedOptimizer.iteration = 0;
        clonedOptimizer.errorMeanSquaredCurrent = 1.0D / 0.0;
        clonedOptimizer.errorRootMeanSquaredChange = 1.0D / 0.0;
        return clonedOptimizer;
    }

    public LevenbergMarquardt getCloneWithModifiedTargetValues(double[] newTargetVaues, double[] newWeights, boolean isUseBestParametersAsInitialParameters) throws CloneNotSupportedException {
        LevenbergMarquardt clonedOptimizer = this.clone();
        clonedOptimizer.targetValues = (double[]) newTargetVaues.clone();
        clonedOptimizer.weights = (double[]) newWeights.clone();
        if (isUseBestParametersAsInitialParameters && this.done()) {
            clonedOptimizer.initialParameters = this.getBestFitParameters();
        }

        return clonedOptimizer;
    }

    public LevenbergMarquardt getCloneWithModifiedTargetValues(List<Number> newTargetVaues, List<Number> newWeights, boolean isUseBestParametersAsInitialParameters) throws CloneNotSupportedException {
        LevenbergMarquardt clonedOptimizer = this.clone();
        clonedOptimizer.targetValues = numberListToDoubleArray(newTargetVaues);
        clonedOptimizer.weights = numberListToDoubleArray(newWeights);
        if (isUseBestParametersAsInitialParameters && this.done()) {
            clonedOptimizer.initialParameters = this.getBestFitParameters();
        }

        return clonedOptimizer;
    }

    public static enum RegularizationMethod {
        LEVENBERG,
        LEVENBERG_MARQUARDT;

        private RegularizationMethod() {
        }
    }
}


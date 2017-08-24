package io.nem.apps.builders;

import java.util.ArrayList;
import java.util.List;

import org.nem.core.crypto.Signature;
import org.nem.core.model.Account;
import org.nem.core.model.MultisigSignatureTransaction;
import org.nem.core.model.MultisigTransaction;
import org.nem.core.model.Transaction;
import org.nem.core.model.TransactionFeeCalculator;
import org.nem.core.model.TransferTransaction;
import org.nem.core.model.primitive.Amount;
import org.nem.core.time.TimeInstant;
import io.nem.apps.service.Globals;
import io.nem.apps.util.TransactionSenderUtil;

/**
 * The Class MultisigTransactionBuilder.
 */
public class MultisigTransactionBuilder {

	/**
	 * Instantiates a new multisig transaction builder.
	 */
	public MultisigTransactionBuilder() {
	}

	/**
	 * Sender.
	 *
	 * @param sender
	 *            the sender
	 * @return the i sender
	 */
	public ITransaction sender(Account sender) {
		return new MultisigTransactionBuilder.Builder(sender);
	}

	public interface ITransaction {

		IBuild otherTransaction(Transaction transaction);
	}

	/**
	 * The Interface IBuild.
	 */
	public interface IBuild {

		IBuild timeStamp(TimeInstant timeInstance);
		
		IBuild signBy(Account account);

		IBuild fee(Amount amount);

		IBuild feeCalculator(TransactionFeeCalculator feeCalculator);

		IBuild deadline(TimeInstant timeInstant);

		IBuild signature(Signature signature);

		IBuild addSignature(MultisigSignatureTransaction signature);

		MultisigTransaction buildMultisigTransaction();

		MultisigTransaction buildAndSendMultisigTransaction();
	}

	/**
	 * The Class Builder.
	 */
	private static class Builder implements ITransaction, IBuild {

		/** The instance. */
		private MultisigTransaction instance;
		
		//	constructor
		private TimeInstant timeStamp;
		private Account sender;
		private Transaction otherTransaction;
		private Signature signature;

		// secondary
		private Amount fee;
		private TransactionFeeCalculator feeCalculator;
		private Account signBy;
		private TimeInstant deadline;
		private List<MultisigSignatureTransaction> multisigSignature = new ArrayList<MultisigSignatureTransaction>();
		
		/**
		 * Instantiates a new builder.
		 *
		 * @param sender
		 *            the sender
		 */
		public Builder(Account sender) {
			this.sender = sender;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.nem.builders.MultisigTransactionBuilder.IBuild#
		 * buildMultisigTransaction()
		 */
		@Override
		public MultisigTransaction buildMultisigTransaction() {
			if (this.timeStamp == null) {
				this.timeStamp = Globals.TIME_PROVIDER.getCurrentTime();
			}
			instance = new MultisigTransaction(this.timeStamp, this.sender, this.otherTransaction);

			if (this.fee == null) {
				TransactionFeeCalculator feeCalculator;
				if (this.feeCalculator != null) {
					feeCalculator = this.feeCalculator;
				} else {
					feeCalculator = Globals.getGlobalTransactionFee();
				}
				instance.setFee(feeCalculator.calculateMinimumFee(instance));
			} else {
				instance.setFee(Amount.fromNem(0));
			}

			if(this.deadline != null) {
				instance.setDeadline(this.deadline);
			}else {
				instance.setDeadline(this.timeStamp.addHours(23));
			}
			if (this.signature != null) {
				instance.setSignature(this.signature);
			} 
			if (this.signBy != null) {
				instance.signBy(this.signBy);
			}
			
			if(this.multisigSignature.size() > 0) {
				for(MultisigSignatureTransaction multiSigSignatureTransaction:this.multisigSignature) {
					instance.addSignature(multiSigSignatureTransaction);
				}
			}
			instance.sign();
			return instance;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.nem.builders.MultisigTransactionBuilder.IBuild#
		 * buildAndSendMultisigTransaction()
		 */
		@Override
		public MultisigTransaction buildAndSendMultisigTransaction() {
			return TransactionSenderUtil.sendMultiSigTransaction(this.buildMultisigTransaction());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * io.nem.builders.MultisigTransactionBuilder.IBuild#fee(org.nem.core.
		 * model.primitive.Amount)
		 */
		@Override
		public IBuild fee(Amount amount) {
			this.fee = amount;
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * io.nem.builders.MultisigTransactionBuilder.IBuild#deadline(org.nem.
		 * core.time.TimeInstant)
		 */
		@Override
		public IBuild deadline(TimeInstant timeInstant) {
			this.deadline = timeInstant;
			return this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * io.nem.builders.MultisigTransactionBuilder.IBuild#signature(org.nem.
		 * core.crypto.Signature)
		 */
		@Override
		public IBuild signature(Signature signature) {
			this.signature = signature;
			return this;
		}

		@Override
		public IBuild timeStamp(TimeInstant timeInstance) {
			return this;
		}

		@Override
		public IBuild signBy(Account account) {
			this.signBy = account;
			return this;
		}

		@Override
		public IBuild feeCalculator(TransactionFeeCalculator feeCalculator) {
			this.feeCalculator = feeCalculator;
			return this;
		}

		@Override
		public IBuild addSignature(MultisigSignatureTransaction signature) {
			this.multisigSignature.add(signature);
			return this;
		}

		@Override
		public IBuild otherTransaction(Transaction transaction) {
			this.otherTransaction = transaction;
			return this;
		}


	}

}

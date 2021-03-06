package maqetta.server.orion.user;

import java.util.HashMap;
import java.util.Set;

import maqetta.core.server.user.manager.PersonManagerImpl;

import org.davinci.server.user.IPerson;
import org.davinci.server.user.UserException;
import org.eclipse.orion.server.useradmin.User;
import org.eclipse.orion.server.useradmin.UserConstants;
import org.eclipse.orion.server.useradmin.UserServiceHelper;

@SuppressWarnings("restriction")
public class OrionPersonManager extends PersonManagerImpl {

	public OrionPersonManager() {
	}

	static class OrionPersonImpl implements IPerson {
		String email;
		String name;
		String password;

		public OrionPersonImpl(String userName, String password, String email) {
			this.name = userName;
			this.password = password;
			this.email = email;
		}

		public String getEmail() {
			if (this.email != null)
				return this.email;

			User user = getOrionUser(this.getUserID());
			if (user != null) {
				this.email = user.getLogin();
			} else {
				System.err.println("ERROR: OrionPersonImpl.getEmail: user '" + this.getUserID()
						+ "' could not be found in IOrionCredentialsService.");
			}
			return this.email;
		}

		public String getUserID() {
			return name;
		}

		public String getFirstName() {
			// TODO Auto-generated method stub
			return "";
		}

		public String getLastName() {
			// TODO Auto-generated method stub
			return "";
		}
	}

	public IPerson addPerson(String userName, String password, String email) throws UserException {
		assertValidUserId(userName);

		IPerson person = (IPerson) persons.get(userName);
		if (person != null) {
			return person;
		}

		person = new OrionPersonImpl(userName, password, email);
		persons.put(userName, person);

		// Trace out the addition
		System.out.println("INFO: OrionPersonManager.addPerson: user added with following info: userName = "
						+ userName + ", email = " + email);

		return person;
	}

	public IPerson login(String userName, String password) {
		assertValidUserId(userName);

		IPerson person = (IPerson) persons.get(userName);
		if (person != null) {
			return person;
		}
		try {
			return addPerson(userName, null, null);
		} catch (UserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public boolean isValidPassword(String userName, String password) {
		return true;
	}

	protected void savePersons() {
	}

	public IPerson getPerson(String userName) {
		assertValidUserId(userName);

		IPerson person = (IPerson) persons.get(userName);
		if (person != null)
			return person;

		try {
			return addPerson(userName, null, null);
		} catch (UserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public IPerson getPersonByEmail(String email) {
		User user = getOrionUserByEmail(email);
		if (user != null) {
			try {
				return addPerson(user.getUid(), null, email);
			} catch (UserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/* no need to load the users */
	protected void loadUsers() {
	}

	public IPerson[] getPersons(String email, int resultNumber, int start) {
		HashMap<String, IPerson> users = new HashMap<String, IPerson>();
		Set<String> names = persons.keySet();
		int i = 0;
		for (String name : names) {
			String personEmail = ((IPerson) persons.get(name)).getEmail();
			if (name.indexOf(email) >= 0 || personEmail.indexOf(email) >= 0) {
				if (i >= start && i < start + resultNumber) {
					users.put(personEmail, new OrionPersonImpl(name, "", personEmail));
				}
				i++;
			}
		}
		return users.values().toArray(new IPerson[0]);
	}

	public String getPhotoRepositoryPath() {
		return "not-implemented";
	}

	private static void assertValidUserId(String uid) {
		if (uid.indexOf("@") != -1) {
			throw new Error("Invalid user ID");
		}
	}

	protected static User getOrionUser(String key, String val) {
		return UserServiceHelper.getDefault().getUserStore().getUser(key, val);
	}

	protected static User getOrionUser(String userName) {
		assertValidUserId(userName);
		return getOrionUser(UserConstants.KEY_UID, userName);
	}

	protected static User getOrionUserByEmail(String email) {
		return getOrionUser(UserConstants.KEY_LOGIN, email);
	}
}

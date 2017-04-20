package com.google.firebase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.firebase.auth.FirebaseCredential;
import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.auth.TestOnlyImplFirebaseAuthTrampolines;
import com.google.firebase.database.TestHelpers;
import com.google.firebase.tasks.OnSuccessListener;
import com.google.firebase.tasks.Task;
import com.google.firebase.tasks.Tasks;
import com.google.firebase.testing.ServiceAccount;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import org.junit.Test;

/** 
 * Tests for {@link FirebaseOptions}.
 */
public class FirebaseOptionsTest {

  private static final String FIREBASE_DB_URL = "https://ghconfigtest-644f2.firebaseio.com";

  private static final FirebaseOptions ALL_VALUES_OPTIONS =
      new FirebaseOptions.Builder()
          .setDatabaseUrl(FIREBASE_DB_URL)
          .setCredential(FirebaseCredentials.fromCertificate(ServiceAccount.EDITOR.asStream()))
          .build();

  @Test
  public void createOptionsWithAllValuesSet() throws IOException, InterruptedException {
    final Semaphore semaphore = new Semaphore(0);
    FirebaseOptions firebaseOptions =
        new FirebaseOptions.Builder()
            .setDatabaseUrl(FIREBASE_DB_URL)
            .setCredential(FirebaseCredentials.fromCertificate(ServiceAccount.EDITOR.asStream()))
            .build();
    assertEquals(FIREBASE_DB_URL, firebaseOptions.getDatabaseUrl());
    TestOnlyImplFirebaseAuthTrampolines.getCertificate(firebaseOptions.getCredential())
        .addOnSuccessListener(
            new OnSuccessListener<GoogleCredential>() {
              @Override
              public void onSuccess(GoogleCredential googleCredential) {
                assertEquals(
                    ServiceAccount.EDITOR.getEmail(), googleCredential.getServiceAccountId());
                semaphore.release();
              }
            });
    TestHelpers.waitFor(semaphore);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void createOptionsWithServiceAccountSet() throws IOException, InterruptedException {
    final Semaphore semaphore = new Semaphore(0);
    FirebaseOptions firebaseOptions =
        new FirebaseOptions.Builder()
            .setDatabaseUrl(FIREBASE_DB_URL)
            .setServiceAccount(ServiceAccount.EDITOR.asStream())
            .build();
    TestOnlyImplFirebaseAuthTrampolines.getCertificate(firebaseOptions.getCredential())
        .addOnSuccessListener(
            new OnSuccessListener<GoogleCredential>() {
              @Override
              public void onSuccess(GoogleCredential googleCredential) {
                assertEquals(
                    ServiceAccount.EDITOR.getEmail(), googleCredential.getServiceAccountId());
                semaphore.release();
              }
            });
    TestHelpers.waitFor(semaphore);
  }

  @Test
  public void createOptionsWithOnlyMandatoryValuesSet() throws IOException, InterruptedException {
    final Semaphore semaphore = new Semaphore(0);
    FirebaseOptions firebaseOptions =
        new FirebaseOptions.Builder()
            .setCredential(FirebaseCredentials.fromCertificate(ServiceAccount.EDITOR.asStream()))
            .build();
    TestOnlyImplFirebaseAuthTrampolines.getCertificate(firebaseOptions.getCredential())
        .addOnSuccessListener(
            new OnSuccessListener<GoogleCredential>() {
              @Override
              public void onSuccess(GoogleCredential googleCredential) {
                try {
                  assertEquals(
                      GoogleCredential.fromStream(ServiceAccount.EDITOR.asStream())
                          .getServiceAccountId(),
                      googleCredential.getServiceAccountId());
                  semaphore.release();
                } catch (IOException e) {
                  fail();
                }
              }
            });
    TestHelpers.waitFor(semaphore);
  }

  @Test
  public void createOptionsWithServiceAccountSetsProjectId() throws Exception {
    FirebaseOptions firebaseOptions =
        new FirebaseOptions.Builder()
            .setCredential(FirebaseCredentials.fromCertificate(ServiceAccount.EDITOR.asStream()))
            .build();
    Task<String> projectId =
        TestOnlyImplFirebaseAuthTrampolines.getProjectId(firebaseOptions.getCredential());
    assertEquals("mock-project-id", Tasks.await(projectId));
  }

  @Test(expected = IllegalStateException.class)
  public void createOptionsWithCredentialMissing() {
    new FirebaseOptions.Builder().build();
  }

  @Test(expected = IllegalStateException.class)
  @SuppressWarnings("deprecation")
  public void createOptionsWithServiceAccountAndCredential() {
    new FirebaseOptions.Builder()
        .setServiceAccount(ServiceAccount.EDITOR.asStream())
        .setCredential(FirebaseCredentials.fromCertificate(ServiceAccount.EDITOR.asStream()))
        .build();
  }

  @Test
  public void checkToBuilderCreatesNewEquivalentInstance() {
    FirebaseOptions allValuesOptionsCopy = new FirebaseOptions.Builder(ALL_VALUES_OPTIONS).build();
    assertNotSame(ALL_VALUES_OPTIONS, allValuesOptionsCopy);
    assertEquals(ALL_VALUES_OPTIONS, allValuesOptionsCopy);
  }

  @Test
  public void testEquals() {
    FirebaseCredential credential = FirebaseCredentials
        .fromCertificate(ServiceAccount.EDITOR.asStream());
    FirebaseOptions options1 =
        new FirebaseOptions.Builder()
            .setCredential(credential)
            .build();
    FirebaseOptions options2 =
        new FirebaseOptions.Builder()
            .setCredential(credential)
            .build();
    assertTrue(options1.equals(options2));
  }

  @Test
  public void testNotEquals() {
    FirebaseCredential credential = FirebaseCredentials
        .fromCertificate(ServiceAccount.EDITOR.asStream());
    FirebaseOptions options1 =
        new FirebaseOptions.Builder()
            .setCredential(credential)
            .build();
    FirebaseOptions options2 =
        new FirebaseOptions.Builder()
            .setCredential(credential)
            .setDatabaseUrl("https://test.firebaseio.com")
            .build();
    assertFalse(options1.equals(options2));
  }

  @Test
  public void testHashCode() {
    FirebaseCredential credential = FirebaseCredentials
        .fromCertificate(ServiceAccount.EDITOR.asStream());
    FirebaseOptions options1 =
        new FirebaseOptions.Builder()
            .setCredential(credential)
            .setDatabaseUrl("https://test.firebaseio.com")
            .build();
    FirebaseOptions options2 =
        new FirebaseOptions.Builder()
            .setCredential(credential)
            .setDatabaseUrl("https://test.firebaseio.com")
            .build();
    FirebaseOptions options3 =
        new FirebaseOptions.Builder()
            .setCredential(credential)
            .setDatabaseUrl("https://test2.firebaseio.com")
            .build();
    assertEquals(options1.hashCode(), options2.hashCode());
    assertNotEquals(options1.hashCode(), options3.hashCode());
  }
}
package auth;

import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
	
	private UserDatabaseServiceGrpc.UserDatabaseServiceBlockingStub mockDb;
	private AuthServiceImpl authService;
	
	@BeforeEach
	void setup() {
		mockDb = mock(UserDatabaseServiceGrpc.UserDatabaseServiceBlockingStub.class);
		authService = new AuthServiceImpl(mockDb);
	}
	
	@Test
	void testValidRegistration() {
		UserRequest request = UserRequest.newBuilder().setUsername("alignie").setEmail("alignie@example.com").build();
		when(mockDb.saveUser(request)).thenReturn(UserResponse.newBuilder().setStatus("OK").build());
		
		TestObserver observer = new TestObserver();
		authService.register(request, observer);
		
		assertEquals("OK", observer.lastResponse.getStatus());
	}
	
	@Test
	void testEmptyEmail() {
		UserRequest request = UserRequest.newBuilder().setUsername("noemail").setEmail("").build();
		
		TestObserver observer = new TestObserver();
		authService.register(request, observer);
		
		assertEquals("Email is required", observer.lastResponse.getStatus());
	}
	
	@Test
	void testDuplicateRegistration() {
		UserRequest request = UserRequest.newBuilder().setUsername("existing").setEmail("existing@example.com").build();
		when(mockDb.saveUser(request)).thenReturn(UserResponse.newBuilder().setStatus("Already exists").build());
		
		TestObserver observer = new TestObserver();
		authService.register(request, observer);
		
		assertEquals("Already exists", observer.lastResponse.getStatus());
	}
	
	@Test
	void testDbFailure() {
		UserRequest request = UserRequest.newBuilder().setUsername("fail").setEmail("fail@example.com").build();
		when(mockDb.saveUser(request)).thenReturn(UserResponse.newBuilder().setStatus("DB Error").build());
		
		TestObserver observer = new TestObserver();
		authService.register(request, observer);
		
		assertEquals("DB Error", observer.lastResponse.getStatus());
	}
	
	@Test
	void testWeirdCharacters() {
		UserRequest request = UserRequest.newBuilder().setUsername("\u7389\u4573").setEmail("naviv@gmail.com").build();
		when(mockDb.saveUser(request)).thenReturn(UserResponse.newBuilder().setStatus("OK").build());
		
		TestObserver observer = new TestObserver();
		authService.register(request, observer);
		
		assertEquals("OK", observer.lastResponse.getStatus());
	}
	
	static class TestObserver implements StreamObserver<UserResponse> {
		UserResponse lastResponse;
		
		@Override
		public void onNext(UserResponse value) {
			this.lastResponse = value;
		}
		
		@Override
		public void onError(Throwable t) {
			fail("gRPC call failed: " + t.getMessage());
		}
		
		@Override
		public void onCompleted() {}
	}
}

package auth;

import io.grpc.stub.StreamObserver;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
	private final UserDatabaseServiceGrpc.UserDatabaseServiceBlockingStub dbStub;
	
	public AuthServiceImpl(UserDatabaseServiceGrpc.UserDatabaseServiceBlockingStub dbStub) {
		this.dbStub = dbStub;
	}
	
	@Override
	public void register(UserRequest request, StreamObserver<UserResponse> responseObserver) {
		if (request.getEmail().isEmpty()) {
			responseObserver.onNext(UserResponse.newBuilder().setStatus("Email is required").build());
			responseObserver.onCompleted();
			return;
		}
		
		UserResponse dbResponse = dbStub.saveUser(request);
		
		responseObserver.onNext(dbResponse);
		responseObserver.onCompleted();
	}
}


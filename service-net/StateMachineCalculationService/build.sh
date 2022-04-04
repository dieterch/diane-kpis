# -------------------------------------------------------------------------

source scl_source enable rh-dotnet21
cd service-net/StateMachineCalculationService
dotnet restore
dotnet publish -c Release -o out
cp ./Dockerfile.build ./out/Dockerfile

# Stage 1 successful


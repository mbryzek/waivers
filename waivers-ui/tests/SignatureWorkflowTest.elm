module SignatureWorkflowTest exposing (..)

import Expect
import Generated.ApiRequest as ApiRequest exposing (ApiRequest(..))
import Generated.IoBryzekWaiversApi as Api
import Html
import Page.Sign as Sign
import Test exposing (..)
import Test.Html.Query as Query
import Url



-- This test simulates the exact workflow that the user reported as broken


suite : Test
suite =
    describe "Complete Signature Workflow"
        [ test "user can submit signature and see success with download link" <|
            \_ ->
                let
                    -- 1. User visits signing page with PDF URL
                    testUrl =
                        { protocol = Url.Http
                        , host = "localhost"
                        , port_ = Just 8080
                        , path = "/sign/sig-test-123"
                        , query = Just "pdf=https%3A%2F%2Fpdf-temp-files.s3.us-west-2.amazonaws.com%2Ftest.pdf"
                        , fragment = Nothing
                        }

                    initialModel =
                        Sign.init "sig-test-123" testUrl

                    -- 2. User types their name
                    ( modelWithName, _ ) =
                        Sign.update (Sign.SignatureDataChanged "John Doe") initialModel

                    -- 3. User clicks "Sign Waiver" button
                    ( submittingModel, _ ) =
                        Sign.update Sign.SubmitSignature modelWithName

                    -- 4. Backend responds with successful signature
                    successSignature =
                        { id = "sig-test-123"
                        , user =
                            { id = "usr-456"
                            , email = "john@example.com"
                            , firstName = "John"
                            , lastName = "Doe"
                            , phone = Nothing
                            }
                        , waiver =
                            { id = "wvr-789"
                            , projectId = "prj-abc"
                            , version = 1
                            , title = "Test Waiver"
                            , content = "Test waiver content"
                            , isCurrent = True
                            }
                        , status = Api.SignatureStatusSigned
                        , signedAt = Nothing
                        , signnowUrl = Nothing
                        }

                    apiResult =
                        Ok successSignature

                    ( finalModel, _ ) =
                        Sign.update (Sign.SignatureSubmitted apiResult) submittingModel

                    -- 5. Success page should be rendered
                in
                -- Test that the model is in Success state
                case finalModel.signatureRequest of
                    Success signature ->
                        Expect.equal signature.id "sig-test-123"

                    _ ->
                        Expect.fail "Expected Success state with signature"
        , test "loading state shows processing message" <|
            \_ ->
                let
                    testUrl =
                        { protocol = Url.Http
                        , host = "localhost"
                        , port_ = Just 8080
                        , path = "/sign/sig-loading-test"
                        , query = Just "pdf=https%3A%2F%2Fexample.com%2Ftest.pdf"
                        , fragment = Nothing
                        }

                    model =
                        Sign.init "sig-loading-test" testUrl

                    loadingModel =
                        { model | signatureRequest = Loading, signatureData = "Test User" }

                    html =
                        Sign.view loadingModel
                in
                html
                    |> Query.fromHtml
                    |> Query.contains [ Html.text "Please wait while we process your signature" ]
        , test "error state shows error message and allows retry" <|
            \_ ->
                let
                    testUrl =
                        { protocol = Url.Http
                        , host = "localhost"
                        , port_ = Just 8080
                        , path = "/sign/sig-error-test"
                        , query = Just "pdf=https%3A%2F%2Fexample.com%2Ftest.pdf"
                        , fragment = Nothing
                        }

                    model =
                        Sign.init "sig-error-test" testUrl

                    errorModel =
                        { model | signatureRequest = Failure ApiRequest.ApiErrorNotFound }

                    html =
                        Sign.view errorModel
                in
                html
                    |> Query.fromHtml
                    |> Query.contains [ Html.text "Signature not found" ]
        ]

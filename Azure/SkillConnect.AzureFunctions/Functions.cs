using Azure;
using Azure.Communication;
using Azure.Communication.Identity;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Extensions.Http;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using SkillConnect.AzureFunctions.Models;
using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;

namespace SkillConnect.AzureFunctions
{
    public class Functions
    {
        string endpoint;
        string accessKey;
        CommunicationIdentityClient client;

        private static readonly List<CommunicationTokenScope> scopes = new List<CommunicationTokenScope>() { CommunicationTokenScope.Chat };

        public Functions(IConfiguration configuration)
        {
#if DEBUG
            this.endpoint = configuration.GetValue<string>("AzureCommunicationEndpoint");
            this.accessKey = configuration.GetValue<string>("AzureCommunicationAccessToken");
#else
            this.endpoint = Environment.GetEnvironmentVariable("AzureCommunicationEndpoint");
            this.accessKey = Environment.GetEnvironmentVariable("AzureCommunicationAccessToken");
#endif
            this.client = new CommunicationIdentityClient(new Uri(endpoint), new AzureKeyCredential(accessKey));
        }

        [FunctionName("GetCommunicationUser")]
        public async Task<IActionResult> GetCommunicationUser(
            [HttpTrigger(AuthorizationLevel.Function, "get", Route = null)] HttpRequest req,
            ILogger log)
        {
            log.LogInformation("[GetCommunicationUser] Executing...");

            var user = client.CreateUser().Value;

            var accessToken = client.GetToken(user, scopes).Value;

            var response = new CommunicationUserDTO()
            {
                AccessToken = accessToken.Token,
                Id = user.Id,
            };

            log.LogInformation("[GetCommunicationUser] Finished...");

            return new OkObjectResult(response);
        }

        [FunctionName("RefreshAccessToken")]
        public async Task<IActionResult> RefreshAccessToken(
            [HttpTrigger(AuthorizationLevel.Function, "get", Route = null)] HttpRequest req,
            ILogger log)
        {
            log.LogInformation("[RefreshAccessToken] Executing...");

            string communicationUserId = req.Query["communicationUserId"];

            string requestBody = await new StreamReader(req.Body).ReadToEndAsync();
            dynamic data = JsonConvert.DeserializeObject(requestBody);
            communicationUserId = communicationUserId ?? data?.communicationUserId;

            var identityToRefresh = new CommunicationUserIdentifier(communicationUserId);

            var accessToken = client.GetToken(identityToRefresh, scopes).Value;

            var response = new CommunicationUserDTO()
            {
                AccessToken = accessToken.Token,
                Id = identityToRefresh.Id,
            };

            log.LogInformation("[RefreshAccessToken] Finished.");

            return new OkObjectResult(response);
        }
    }
}

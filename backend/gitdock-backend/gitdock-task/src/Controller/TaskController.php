<?php

namespace App\Controller;

use App\Service\TaskService;
use App\Service\TaskMapper;
use App\Repository\EpicRepository;
use App\Repository\LevelRepository;
use App\Repository\UserRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/api/tasks')]
class TaskController extends AbstractController
{
    public function __construct(
        private TaskService $taskService,
        private TaskMapper $taskMapper
    ) {}

    // ================= GET ALL =================
    #[Route('', methods: ['GET'])]
    public function list(): JsonResponse
    {
        $tasks = $this->taskService->getAll();

        return $this->json([
            'success' => true,
            'data' => $tasks
        ]);
    }

    // ================= GET FORM DATA =================
    // 🛡️ Cette route doit absolument être définie AVANT la route /{id} !
    #[Route('/form-data', methods: ['GET'])]
    public function getFormData(
        EpicRepository $epicRepo,
        LevelRepository $levelRepo,
        UserRepository $userRepo
    ): JsonResponse {
        // On récupère et formate les données pour les Selects du frontend Vue.js
        $epics = array_map(fn($e) => ['id' => $e->getId(), 'title' => $e->getTitle()], $epicRepo->findAll());
        $levels = array_map(fn($l) => ['id' => $l->getId(), 'name' => $l->getName()], $levelRepo->findAll());
        $users = array_map(fn($u) => ['id' => $u->getExternalId(), 'fullName' => $u->getFullName()], $userRepo->findAll());

        return $this->json([
            'epics' => $epics,
            'parts' => [], // 'Part' n'est pas encore géré dans ce microservice
            'levels' => $levels,
            'users' => $users
        ]);
    }

    // ================= GET ONE =================
    // 🛡️ FIX : On force l'id à être un nombre avec requirements: ['id' => '\d+']
    #[Route('/{id}', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function show(int $id): JsonResponse
    {
        try {
            $task = $this->taskService->getById($id);

            return $this->json([
                'success' => true,
                'data' => $task
            ]);
        } catch (\Exception $e) {
            return $this->json([
                'success' => false,
                'message' => $e->getMessage()
            ], 404);
        }
    }

    // ================= CREATE =================
    #[Route('', methods: ['POST'])]
    public function create(Request $request): JsonResponse
    {
        try {
            $dto = $this->taskMapper->mapTaskRequestToDto($request);
            $task = $this->taskService->create($dto);

            return $this->json([
                'success' => true,
                'data' => $task
            ], 201);

        } catch (\Exception $e) {
            return $this->json([
                'success' => false,
                'message' => $e->getMessage()
            ], 400);
        }
    }

    // ================= UPDATE =================
    #[Route('/{id}', methods: ['PUT'], requirements: ['id' => '\d+'])]
    public function update(int $id, Request $request): JsonResponse
    {
        try {
            $dto = $this->taskMapper->mapTaskRequestToDto($request);
            $task = $this->taskService->update($id, $dto);

            return $this->json([
                'success' => true,
                'data' => $task
            ]);

        } catch (\Exception $e) {
            return $this->json([
                'success' => false,
                'message' => $e->getMessage()
            ], 400);
        }
    }

    // ================= DELETE =================
    #[Route('/{id}', methods: ['DELETE'], requirements: ['id' => '\d+'])]
    public function delete(int $id): JsonResponse
    {
        try {
            $this->taskService->softDelete($id);

            return $this->json([
                'success' => true,
                'message' => 'Task deleted successfully'
            ]);

        } catch (\Exception $e) {
            return $this->json([
                'success' => false,
                'message' => $e->getMessage()
            ], 400);
        }
    }

    // ================= MARK AS DONE =================
    #[Route('/{id}/done', methods: ['PATCH'], requirements: ['id' => '\d+'])]
    public function done(int $id): JsonResponse
    {
        try {
            $task = $this->taskService->markAsDone($id);

            return $this->json([
                'success' => true,
                'data' => $task
            ]);

        } catch (\Exception $e) {
            return $this->json([
                'success' => false,
                'message' => $e->getMessage()
            ], 400);
        }
    }

    #[Route('/test/auth/{id}', methods: ['GET'], requirements: ['id' => '\d+'])]
    public function testAuth(int $id): JsonResponse
    {
        return $this->json([
            'success' => true,
            'data' => $this->taskService->getById($id)
        ]);
    }
}
